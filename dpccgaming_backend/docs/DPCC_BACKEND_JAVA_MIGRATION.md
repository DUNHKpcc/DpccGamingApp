# DPCC Gaming 后端学习与 Java 迁移文档

本文基于仓库 `https://github.com/DUNHKpcc/DpccGaming_V1.3` 的现有后端实现整理，目标是给 Flutter 客户端提供一套可演进的 Java 后端方案。

## 1. 现有后端技术现状（Node.js）

### 1.1 技术栈
- 运行时: Node.js >= 18
- Web 框架: Express 4
- 数据库: MySQL + `mysql2/promise`
- 认证: JWT (`jsonwebtoken`)
- 密码: `bcryptjs`
- 限流: `express-rate-limit`
- 文件上传: `multer`

关键文件:
- `server.js`
- `backend/config/database.js`
- `backend/middleware/auth.js`
- `backend/controllers/*.js`
- `backend/routes/*.js`

### 1.2 模块职责
- 认证: 注册/登录/JWT 校验/获取当前用户
- 游戏: 列表、详情、上传、播放计数、源码浏览与下载
- 评论: 评论与回复
- 通知: 列表、单条已读、全部已读
- 管理员: 审核游戏、删游戏、用户管理（角色/封禁/删除）
- AI: 代码助手代理转发（第三方模型）
- Debug: 修复源码链接、查询最近游戏

### 1.3 API 路由概览
- `POST /api/register`
- `POST /api/login`
- `GET /api/verify-token`
- `GET /api/user/profile`
- `GET /api/auth/me` / `GET /api/me`
- `GET /api/games`
- `GET /api/games/:gameId`
- `POST /api/games/:gameId/play`
- `GET /api/games/:gameId/code` (需登录)
- `GET /api/games/:gameId/code.zip` (需登录)
- `POST /api/games` (需登录 + multipart)
- `GET /api/games/:gameId/comments`
- `POST /api/games/:gameId/comments` (需登录)
- `POST /api/games/:gameId/comments/:commentId/reply` (需登录)
- `GET /api/notifications` (需登录)
- `POST /api/notifications/:id/read` (需登录)
- `POST /api/notifications/mark-all-read` (需登录)
- `POST /api/admin/games/:gameId/review` (管理员)
- `GET /api/admin/games/pending` (管理员)
- `GET /api/admin/games/all` (管理员)
- `DELETE /api/admin/games/:gameId/delete` (管理员)
- `GET /api/admin/check-permission` (管理员)
- `GET /api/admin/users` (管理员)
- `POST /api/admin/users/:userId/role` (管理员)
- `POST /api/admin/users/:userId/ban` (管理员)
- `DELETE /api/admin/users/:userId/delete` (管理员)
- `POST /api/ai/code-assistant`
- `GET /api/debug/fix-code-urls` (管理员)
- `GET /api/debug/games`

### 1.4 数据库结构（MySQL）
来自 `integrated_database.sql` 和 `updateSql1.sql`，核心表:
- `users`: 用户、角色、状态
- `games`: 游戏信息、审核信息、上传人、资源 URL、统计信息
- `comments`: 评论、评分、回复关系
- `notifications`: 通知信息（关联用户/游戏/评论）

补丁字段:
- `games.engine`
- `games.code_type`
- `games.video_url`

### 1.5 现有后端值得保留的设计
- 业务边界明确: auth/game/comment/admin/notification 分离
- 文件与元数据分离: 文件落盘，数据库仅存 URL 和索引字段
- 管理端操作基本有事务保护（删用户/删游戏）

### 1.6 现有后端风险点（迁移时建议修复）
- 配置中出现默认明文敏感信息（DB 密码、JWT Secret、AI Key）
- `getGameComments` 存在 N+1 查询模式（主评论后逐条查回复）
- 部分逻辑耦合文件系统和控制器，后期扩展云存储成本高
- 返回结构不完全统一（有的 `{ user: ... }`，有的直接对象）
- 缺少统一参数校验层和统一业务错误码体系

## 2. Java 后端目标方案（给 Flutter 用）

## 2.1 建议技术栈
- Java 21
- Spring Boot 3.x
- Spring Web / Validation / Security
- Spring Data JPA（或 MyBatis-Plus，二选一）
- MySQL 8
- Flyway（数据库迁移）
- Redis（可选: 缓存、限流、登录态增强）
- MinIO 或本地磁盘（文件存储）

## 2.2 推荐目录结构
建议在 `dpcc_gaming_backend` 采用:

```text
src/main/java/com/dpccgaming/backend/
  common/           # 通用响应、异常、工具、枚举
  config/           # 安全、跨域、Jackson、OpenAPI、存储配置
  auth/             # 登录注册、JWT、用户上下文
  user/             # 用户资料与管理
  game/             # 游戏、上传、播放计数
  comment/          # 评论与回复
  notification/     # 通知
  admin/            # 管理员审核/运营能力
  ai/               # AI代理服务
  debug/            # 调试接口（生产建议关闭）
```

## 2.3 推荐分层规范
- `Controller`: 只接收参数/返回 DTO，不写业务细节
- `Service`: 业务编排、事务边界
- `Repository/Mapper`: 数据访问
- `Entity`: 持久化模型
- `DTO/VO`: API 输入输出

## 2.4 API 兼容策略（重要）
Flutter 初期迁移建议保持与旧接口兼容:
- 保持 URL 不变（`/api/...`）
- 保持关键字段名不变（如 `game_id`, `code_package_url`）
- 保持核心响应结构不变（降低 Flutter 联调成本）

后续再做 `/api/v2` 渐进式重构。

## 3. Java 模块设计映射

### 3.1 Auth 模块
接口:
- `POST /api/register`
- `POST /api/login`
- `GET /api/verify-token`
- `GET /api/user/profile`

实现要点:
- BCrypt 哈希
- JWT + 过期时间
- 统一认证过滤器（`Bearer Token`）
- 被封禁用户直接拒绝访问

### 3.2 Game 模块
接口:
- `GET /api/games`
- `GET /api/games/{gameId}`
- `POST /api/games/{gameId}/play`
- `POST /api/games`（multipart）
- `GET /api/games/{gameId}/code`
- `GET /api/games/{gameId}/code.zip`

实现要点:
- 上传后异步解压与校验（至少存在 `index.html`）
- 文件系统抽象为 `StorageService`
- 代码浏览限制文件数、大小、后缀
- 列表查询聚合评分、评论数（SQL 优化）

### 3.3 Comment 模块
接口:
- `GET /api/games/{gameId}/comments`
- `POST /api/games/{gameId}/comments`
- `POST /api/games/{gameId}/comments/{commentId}/reply`

实现要点:
- 评论与回复分层查询，避免 N+1
- 写入后触发通知事件（域事件或 service 调用）
- 更新 `games.rating_avg`

### 3.4 Notification 模块
接口:
- `GET /api/notifications`
- `POST /api/notifications/{id}/read`
- `POST /api/notifications/mark-all-read`

实现要点:
- 分页查询（`page/limit`）
- 只允许用户操作自己的通知

### 3.5 Admin 模块
接口:
- 游戏审核、删除
- 用户列表、改角色、封禁、删除

实现要点:
- RBAC（`ROLE_ADMIN`, `ROLE_SUPER_ADMIN`）
- 高危操作必须事务化
- 可增加操作审计日志（建议）

### 3.6 AI 模块
接口:
- `POST /api/ai/code-assistant`

实现要点:
- API Key 严禁硬编码
- 设置超时、重试、熔断
- 增加输入长度限制和敏感内容过滤

## 4. 数据库迁移建议（Flyway）

将 SQL 拆分为版本化脚本:
- `V1__init_tables.sql`
- `V2__add_user_role_status.sql`
- `V3__add_game_review_fields.sql`
- `V4__add_comment_reply_fields.sql`
- `V5__create_notifications.sql`
- `V6__add_game_engine_code_video.sql`

并额外补充:
- 唯一约束/索引统一命名
- 删除不再适用的唯一约束（如评论是否允许同用户多条）
- 为高频查询增加复合索引（例如 `games(status, created_at)`）

## 5. Flutter + Java 联调规范

建议统一响应结构:

```json
{
  "code": "OK",
  "message": "success",
  "data": {}
}
```

兼容期可暂保留旧结构，并在网关层或适配层转换。

建议统一错误码:
- `AUTH_TOKEN_INVALID`
- `AUTH_FORBIDDEN`
- `USER_BANNED`
- `GAME_NOT_FOUND`
- `GAME_UPLOAD_INVALID_PACKAGE`
- `COMMENT_INVALID_RATING`

## 6. 分阶段落地计划（建议）

1. 第 1 阶段: 认证 + 用户资料 + 游戏列表/详情（只读）
2. 第 2 阶段: 评论/回复 + 通知
3. 第 3 阶段: 上传、源码浏览下载、审核后台
4. 第 4 阶段: AI 代理、监控告警、性能优化

每阶段都做:
- 接口契约测试
- 与旧 Node 接口对照测试
- Flutter 端灰度切换（按模块切流）

## 7. 当前仓库建议的下一步

你当前本地已有 `dpcc_gaming_backend`（Maven 骨架），建议马上做:
- 建立 Spring Boot 主工程与基础依赖
- 先实现 `Auth + Games(只读)` 两组接口
- 建立 `docs/api-contract.md`，固定 Flutter 联调字段

这样可以最快进入“可跑通 Flutter 首页 + 登录”的状态。
