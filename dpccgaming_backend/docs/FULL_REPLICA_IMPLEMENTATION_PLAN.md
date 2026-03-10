# DPCC Gaming 后端全量复刻实施文档

## 1. 目标

本文件的目标不是推倒重写，而是基于当前已有的 Java 后端骨架，完整复刻原 Node.js 后端 `/Users/dpccskisw/Documents/DpccProject/DpccGaming/backend` 的业务能力、接口行为、数据库结构和文件处理流程。

核心原则：

- 不放弃现有 Java 代码，只做增量演进。
- 先保证接口兼容和功能复刻，再做优化和重构。
- 先完成单体后端，不在复刻阶段引入微服务拆分。
- 优先采用企业里稳定、常用、维护成本低的技术。

---

## 2. 原 Node 后端复刻范围

当前需要复刻的 Node 后端位于：

- `/Users/dpccskisw/Documents/DpccProject/DpccGaming/backend/controllers`
- `/Users/dpccskisw/Documents/DpccProject/DpccGaming/backend/routes`
- `/Users/dpccskisw/Documents/DpccProject/DpccGaming/backend/middleware`
- `/Users/dpccskisw/Documents/DpccProject/DpccGaming/backend/utils`
- `/Users/dpccskisw/Documents/DpccProject/DpccGaming/database/integrated_database.sql`

### 2.1 已确认的业务模块

1. 认证模块
2. 游戏模块
3. 评论模块
4. 通知模块
5. 管理员模块
6. AI 代码助手模块
7. Debug 模块

### 2.2 已确认的接口清单

认证：

- `POST /api/register`
- `POST /api/login`
- `GET /api/verify-token`
- `GET /api/user/profile`
- `GET /api/auth/me`
- `GET /api/me`

游戏：

- `GET /api/games`
- `GET /api/games/:gameId`
- `POST /api/games/:gameId/play`
- `GET /api/games/:gameId/code`
- `GET /api/games/:gameId/code.zip`
- `POST /api/games`

评论：

- `GET /api/games/:gameId/comments`
- `POST /api/games/:gameId/comments`
- `POST /api/games/:gameId/comments/:commentId/reply`

通知：

- `GET /api/notifications`
- `POST /api/notifications/:id/read`
- `POST /api/notifications/mark-all-read`

管理员：

- `POST /api/admin/games/:gameId/review`
- `GET /api/admin/games/pending`
- `GET /api/admin/games/all`
- `DELETE /api/admin/games/:gameId/delete`
- `GET /api/admin/check-permission`
- `GET /api/admin/users`
- `POST /api/admin/users/:userId/role`
- `POST /api/admin/users/:userId/ban`
- `DELETE /api/admin/users/:userId/delete`

AI / Debug：

- `POST /api/ai/code-assistant`
- `GET /api/debug/fix-code-urls`
- `GET /api/debug/games`

---

## 3. 原 Node 后端的实现细节总结

### 3.1 认证

原实现特征：

- 使用 `bcryptjs` 做密码哈希。
- 使用 JWT，过期时间 24 小时。
- token 中保存 `userId` 和 `username`。
- 登录和注册返回结构不是统一包装体，而是直接 JSON。
- `/api/user/profile` 返回裸用户对象。
- `/api/auth/me` 和 `/api/me` 返回 `{ user: ... }`。

Java 复刻要求：

- 保持旧 URL。
- 保持旧字段和主要响应结构。
- 后续可以在 Java 内部统一到 `ApiResponse`，但对 Flutter 兼容期内应提供适配层。

### 3.2 游戏

原实现特征：

- 游戏上传使用 multipart，字段包括 `gameFile`、`video`、`codeArchive`。
- 游戏包必须是 ZIP。
- 游戏 ZIP 解压后必须能找到 `index.html`。
- 视频文件保存到 `uploads/video`。
- 源码压缩包复制到 `uploads/code/{gameId}.zip`，并解压到 `uploads/code/{gameId}`。
- 游戏浏览地址来自真实文件解压路径。
- 游戏列表、详情接口会拼装：
  - `average_rating`
  - `comment_count`
  - `play_count`
  - `code_package_url`
  - `code_exists`
- 源码浏览接口只暴露特定后缀，限制文件数量和单文件大小。

Java 复刻要求：

- 保留本地磁盘存储模式。
- 先完整复刻文件系统行为，再抽象 `StorageService`。
- 先兼容 `games/` 和 `uploads/code/` 目录布局。

### 3.3 评论

原实现特征：

- 主评论与回复共用 `comments` 表。
- 主评论 `parent_id IS NULL`。
- 回复通过 `parent_id` 和 `reply_to_user_id` 表达。
- 新增评论后回写 `games.rating_avg`。
- 回复别人时创建通知。

Java 复刻要求：

- 表结构保持兼容。
- 允许同用户对同一游戏多条评论，不能沿用旧唯一约束逻辑。
- 评论读取建议改成两段查询，避免 Node 当前的 N+1 模式。

### 3.4 通知

原实现特征：

- 通知类型有 `game_approved`、`game_rejected`、`comment_reply`。
- 只允许操作自己的通知。
- 支持分页读取。

Java 复刻要求：

- 保留通知类型枚举值。
- 保留分页字段 `page`、`limit`、`total`、`hasMore`。

### 3.5 管理员

原实现特征：

- 管理员权限来自用户表 `role` 与 `status`。
- 普通管理员不能禁言/删除其他管理员。
- 超级管理员可以操作管理员。
- 删除用户、删除游戏都使用事务。
- 删除数据库数据时，同时尝试删除磁盘文件。

Java 复刻要求：

- Java 侧必须保留事务边界。
- RBAC 不能只放在路由层，服务层也要做校验。

### 3.6 AI 代码助手

原实现特征：

- 调用外部大模型接口。
- 传入 prompt、当前文件、文件内容、文件列表。
- 截断上下文长度。
- 直接代理外部 HTTP 服务。

Java 复刻要求：

- 先复刻能力，不改变业务入口。
- API Key 必须走环境变量，不允许继续保留默认明文值。
- 加超时、重试、熔断、审计日志。

### 3.7 Debug

原实现特征：

- 修复数据库中 `code_package_url`。
- 查询最近游戏记录。

Java 复刻要求：

- Debug 接口保留，但生产环境可通过 profile 或开关关闭。

---

## 4. 数据库复刻范围

原 SQL 基线来自：

- `/Users/dpccskisw/Documents/DpccProject/DpccGaming/database/integrated_database.sql`

### 4.1 核心表

- `users`
- `games`
- `comments`
- `notifications`

### 4.2 关键字段

`users`：

- `username`
- `password_hash`
- `email`
- `role`
- `status`

`games`：

- `game_id`
- `title`
- `description`
- `category`
- `thumbnail_url`
- `video_url`
- `game_url`
- `code_package_url`
- `engine`
- `code_type`
- `play_count`
- `rating_avg`
- `status`
- `uploaded_by`
- `uploaded_at`
- `reviewed_by`
- `reviewed_at`
- `review_notes`

`comments`：

- `user_id`
- `game_id`
- `rating`
- `comment_text`
- `parent_id`
- `reply_to_user_id`

`notifications`：

- `user_id`
- `type`
- `title`
- `content`
- `related_game_id`
- `related_comment_id`
- `is_read`

### 4.3 数据库迁移建议

不要把 Node 的整合 SQL 继续原样塞进 Java 项目中。应改为 Flyway 版本化脚本，例如：

1. `V1__create_users.sql`
2. `V2__create_games.sql`
3. `V3__create_comments.sql`
4. `V4__add_user_role_status.sql`
5. `V5__add_game_review_fields.sql`
6. `V6__add_comment_reply_fields.sql`
7. `V7__create_notifications.sql`
8. `V8__add_game_engine_code_video.sql`
9. `V9__fix_indexes_and_constraints.sql`

这样做的原因：

- 便于审计数据库变更。
- 便于测试环境和生产环境同步。
- 便于后续继续演进，不会再出现整库脚本难维护的问题。

---

## 5. 现有 Java 代码中应保留的部分

当前 Java 项目不是废代码，应该保留并继续扩展。

建议保留：

- `pom.xml`
- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml`
- `src/main/java/com/dpccgaming/backend/common/api/ApiResponse.java`
- `src/main/java/com/dpccgaming/backend/common/exception/BusinessException.java`
- `src/main/java/com/dpccgaming/backend/auth/entity/User.java`
- `src/main/java/com/dpccgaming/backend/auth/repository/UserMapper.java`
- `src/main/java/com/dpccgaming/backend/auth/service/AuthService.java`
- `src/main/java/com/dpccgaming/backend/auth/service/AuthServiceImpl.java`
- `src/main/java/com/dpccgaming/backend/config/SecurityBeanConfig.java`
- `src/main/java/com/dpccgaming/backend/config/SecurityConfig.java`

### 5.1 保留方式

不是“原封不动地用”，而是“在现有基础上继续长出来”。

例如：

- `SecurityConfig` 继续演进成 JWT 过滤器体系，不要删掉重写成另一套风格。
- `AuthServiceImpl` 继续扩展登录、验 token、获取 profile，不要另起一套平行 AuthService。
- `UserMapper` 继续作为 `users` 表访问入口。
- `ApiResponse` 可继续保留，兼容期增加适配逻辑即可。

### 5.2 需要修正但不需要推倒的点

- DTO 命名里的 `ReigisterRequest` 应统一改为 `RegisterRequest`。
- `SecurityBeanConfig` 中 bean 命名需要规范化。
- `SecurityConfig` 当前还是 HTTP Basic 思路，需要替换为 Bearer JWT。
- `AuthController` 当前还没有真正调用注册业务。

---

## 6. 推荐技术路线

## 6.1 当前项目应该坚持的主路线

这是本项目最合适的技术组合：

- Java 21
- Spring Boot 3.4.x 或后续平滑升级到 3.5.x
- Spring MVC
- Spring Security
- MyBatis-Plus
- MySQL 8
- Flyway
- Maven

原因：

- 你当前项目已经在 Java 21 + Spring Boot 3.4.3 上起步，迁移成本最低。
- MyBatis-Plus 很适合当前这种“需要严格贴近旧 SQL 和旧表结构”的复刻项目。
- Spring MVC 足够覆盖当前业务，不需要为了“新技术”切到 WebFlux。
- 单体应用更适合当前团队规模和项目阶段。

## 6.2 企业当前更常用的新技术

以下是截至 2026 年 3 月，企业 Java 后端里更常见、也更值得引入到新项目中的技术方向，但要分“现在就用”和“后面再加”。

### A. 现在就值得纳入的

- Java 21 LTS
- Spring Security 方法级鉴权
- Flyway 数据库版本迁移
- Micrometer + OpenTelemetry 可观测性
- Testcontainers 集成测试
- OpenAPI 3 接口文档

### B. 适合第二阶段再纳入的

- 虚拟线程
- Redis 缓存与限流
- Resilience4j 外部依赖保护
- S3/MinIO 存储抽象

### C. 现在不建议为了“新”而上

- 微服务拆分
- WebFlux 全量改造
- 事件总线/消息队列先行
- DDD 大规模建模
- JPA 和 MyBatis-Plus 混用

---

## 7. 技术选型建议：企业常用技术 vs 本项目建议

### 7.1 Java 版本

企业现状：

- Java 21 是当前最稳妥的企业主流 LTS。
- Java 25 已是新的 LTS，但对老项目和现有依赖并不一定值得立即切换。

本项目建议：

- 保持 Java 21，不升级到 Java 25。

理由：

- 当前代码已经是 Java 21。
- 复刻阶段优先稳定和兼容。
- Java 21 已经包含虚拟线程等现代能力。

### 7.2 Spring Boot

企业现状：

- Spring Boot 3.x 仍是企业主流。
- 新项目可以关注 3.5.x，Boot 4.0 属于新大版本，不适合在复刻阶段贸然切换。

本项目建议：

- 先保留当前 3.4.3。
- 等接口稳定后，再视需要升级到 3.5.x。

### 7.3 ORM / 数据访问

企业常见选择：

- JPA/Hibernate
- MyBatis
- MyBatis-Plus

本项目建议：

- 继续用 MyBatis-Plus。

理由：

- 旧系统 SQL 逻辑明确，MyBatis-Plus 更适合贴近原逻辑。
- 审核、聚合、评论、通知、批量更新等场景，SQL 可控性更高。
- 对你现有 Java 项目改动最小。

### 7.4 安全

企业常用：

- Spring Security + JWT / OAuth2 Resource Server

本项目建议：

- 当前阶段使用 Spring Security + JWT 自定义过滤器。
- 后续如需要接企业统一身份平台，再考虑 OAuth2 / OIDC。

理由：

- 现有 Node 逻辑本来就是 JWT。
- Flutter 客户端对 JWT 更容易平滑迁移。

### 7.5 文件存储

企业常见：

- 本地磁盘
- MinIO
- AWS S3 / 阿里云 OSS / 腾讯云 COS

本项目建议：

- 第 1 阶段保留本地磁盘。
- 第 2 阶段引入 `StorageService` 抽象。
- 第 3 阶段视部署环境切换 MinIO / OSS。

理由：

- 现在复刻的关键是目录结构和访问路径兼容。
- 过早上对象存储会增加迁移复杂度。

### 7.6 可观测性

企业现状：

- 现代项目普遍开始用指标、追踪、日志联动。

本项目建议：

- 接入 Spring Boot Actuator。
- 使用 Micrometer 暴露 metrics。
- 使用 OpenTelemetry 对接 tracing。

理由：

- 上传、审核、AI 调用、文件解压都需要问题定位能力。
- 可观测性在企业环境里已经不是“高级可选项”，而是标准基础设施。

### 7.7 测试

企业现状：

- 单元测试 + 集成测试 + 容器化测试是主流组合。

本项目建议：

- 保留 `spring-boot-starter-test`。
- 增加 Testcontainers for MySQL。
- Flyway migration 在测试时真实执行。

理由：

- 这个项目对数据库结构和文件路径依赖很重。
- 只写 mock 测试意义不大。

---

## 8. 本项目推荐新增的实现组件

下面这些组件建议加入，但不改变整体技术路线。

### 8.1 必须新增

- JWT 工具类
- JWT 认证过滤器
- Spring Security 用户上下文解析
- 全局异常处理器 `@RestControllerAdvice`
- 参数校验体系 `jakarta.validation`
- Flyway migration 脚本目录
- 游戏、评论、通知、管理员模块的 entity/mapper/service/controller
- 文件存储抽象接口 `StorageService`
- 源码浏览服务 `CodeBrowseService`

### 8.2 强烈建议新增

- OpenAPI 文档
- Actuator
- 审计日志
- 统一错误码枚举
- 事务边界说明
- 集成测试

### 8.3 第二阶段再新增

- Redis
- Resilience4j
- MinIO
- 异步任务处理

---

## 9. 各模块 Java 实现细节建议

## 9.1 Auth 模块

包建议：

- `auth.controller`
- `auth.service`
- `auth.dto`
- `auth.entity`
- `auth.repository`
- `auth.security`

实现建议：

- 注册：用户名唯一校验、密码加密、角色默认 `user`、状态默认 `active`
- 登录：校验用户名密码，返回 token 和用户基本信息
- 验 token：从 JWT 解析用户
- 获取当前用户：兼容 `/api/auth/me` 和 `/api/me`
- 获取资料：兼容 `/api/user/profile`

## 9.2 Game 模块

包建议：

- `game.controller`
- `game.service`
- `game.dto`
- `game.entity`
- `game.repository`
- `game.storage`

实现建议：

- 列表页查询：保留评分均值、评论数、源码状态
- 详情页查询：返回单游戏完整视图
- 上传：必须事务性保存数据库记录，但文件保存与解压要有补偿清理
- 播放计数：直接原子更新
- 源码浏览：限制后缀、文件数、文件大小
- 源码下载：优先返回已保存 zip

## 9.3 Comment 模块

实现建议：

- 评论和回复仍共表
- 查询时不要每条主评论单独查回复
- 提交评论后更新 `games.rating_avg`
- 回复时如果 `replyToUserId != currentUserId`，创建通知

## 9.4 Notification 模块

实现建议：

- 查询仅返回当前用户通知
- 标记已读按用户维度限制
- 全部已读走批量更新

## 9.5 Admin 模块

实现建议：

- 方法级鉴权
- 角色校验放到 service 层
- 删除游戏、删除用户必须事务化
- 数据库删除与文件删除拆分处理，文件失败仅记录日志

## 9.6 AI 模块

实现建议：

- 使用 `RestClient` 或 `WebClient` 调第三方大模型
- 配置超时
- 配置重试和熔断
- 记录调用耗时
- 严禁默认明文 Key

## 9.7 Debug 模块

实现建议：

- 仅在开发或管理员模式开启
- 放到独立 profile 控制

---

## 10. 不建议做的事

- 不要为了“企业架构”先拆微服务。
- 不要在复刻阶段把 MyBatis-Plus 改成 JPA。
- 不要直接改 Flutter 端去适配新的接口字段。
- 不要先做大量抽象再补业务。
- 不要把文件存储一次性切到云对象存储。
- 不要保留 Node 项目里的默认明文密码、JWT Secret、AI Key。

---

## 11. 分阶段实施建议

### 第 1 阶段：认证和基础只读能力

- 完成注册、登录、验 token、用户资料
- 完成游戏列表、详情、播放计数
- 落地 Flyway 基础表结构
- 打通 JWT 鉴权

目标：

- Flutter 能跑登录和首页列表

### 第 2 阶段：评论和通知

- 完成评论、回复、通知
- 补齐评分回写
- 补齐通知分页和已读能力

目标：

- Flutter 能跑详情页评论和消息中心

### 第 3 阶段：上传、源码浏览、后台审核

- 完成游戏上传
- 完成视频和源码文件处理
- 完成源码浏览和下载
- 完成管理员审核、删游戏、用户管理

目标：

- 平台运营闭环跑通

### 第 4 阶段：AI、可观测性、质量提升

- 完成 AI 代理
- 接入 Actuator / Micrometer / OpenTelemetry
- 补全 Testcontainers 集成测试
- 视情况加 Redis / Resilience4j / MinIO

目标：

- 进入企业可维护状态

---

## 12. 结论

这次复刻最合适的路线，不是“重新设计一个现代后端”，而是：

1. 以原 Node 后端为功能基线。
2. 以现有 Java 项目为实施基线。
3. 用 Java 21 + Spring Boot 3.4/3.5 + Spring Security + MyBatis-Plus + MySQL + Flyway 做单体复刻。
4. 用企业当前更常见的可观测性、测试、治理能力逐步增强。

最终建议：

- 保留现有 Java 代码。
- 继续使用 MyBatis-Plus。
- 先完整复刻接口、数据库和文件流程。
- 复刻完成后，再逐步引入 OpenTelemetry、Testcontainers、Redis、Resilience4j、MinIO。

---

## 13. 参考资料

本地代码与数据库基线：

- `/Users/dpccskisw/Documents/DpccProject/DpccGaming/backend`
- `/Users/dpccskisw/Documents/DpccProject/DpccGaming/database/integrated_database.sql`
- `/Users/dpccskisw/dpccgaming_app/dpccgaming_backend/src/main/java`

官方技术资料：

- Spring Boot System Requirements: https://docs.spring.io/spring-boot/system-requirements.html
- Spring Boot Virtual Threads: https://docs.spring.io/spring-boot/reference/features/spring-application.html
- Spring Boot Observability: https://docs.spring.io/spring-boot/reference/actuator/observability.html
- Spring Security Reference: https://docs.spring.io/spring-security/reference/index.html
- Spring Security Method Security: https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html
- MyBatis-Plus Official Site: https://www.baomidou.com/en/
- MyBatis-Plus Stream Query: https://baomidou.com/en/guides/stream-query/
- OpenJDK JDK 21: https://openjdk.org/projects/jdk/21
- Oracle Java Releases: https://www.java.com/en/releases/
- Testcontainers for Java: https://java.testcontainers.org/
- Testcontainers overview: https://testcontainers.com/
- Redgate Flyway Documentation: https://documentation.red-gate.com/flyway
- OpenTelemetry Java: https://opentelemetry.io/docs/languages/java/
