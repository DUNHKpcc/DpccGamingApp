package com.dpccgaming.backend.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dpccgaming.backend.auth.dto.ReigisterRequest;
import com.dpccgaming.backend.auth.entity.User;
import com.dpccgaming.backend.auth.repository.UserMapper;
import com.dpccgaming.backend.common.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void register(ReigisterRequest request) {
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );
        if (existing != null) {
            throw new BusinessException("USER_EXISTS", "用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setIsBanned(0);

        int rows = userMapper.insert(user);
        if (rows != 1) {
            throw new BusinessException("REGISTER_FAILED", "注册失败");
        }
    }

    @Override
    public LoginResponse login(LogingRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername,request.getUsername())
        );

        if(user == null || !passwordEncoder.matches(request.getPassword,user.getPasswordHash())){
            throw new BusinessException("AUTH_INVALID","用户名或密码错误");
        }

        if("banned".equals(user.getStatus())){
            throw new BusinessException("AUTH_INVALID","账户已被禁用");
        }

        String token = jwtTokenProvider.generateToken(user.Id(),user.getUsername());

        return new LoginResponse(
                "登陆成功",
                token,
                new UserInfo(user.getId(),user.getUsername(),user.getEmail())
        );
    }

    @Override
    public VerifyTokenResponse verifyToken(Long userId, String username) {
        return null;
    }

    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        return null;
    }

    @Override
    public CurrentUserResponse getCurrentUser(Long userId) {
        return null;
    }
}
