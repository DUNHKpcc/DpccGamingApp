package com.dpccgaming.backend.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dpccgaming.backend.auth.dto.AuthUserResponse;
import com.dpccgaming.backend.auth.dto.CurrentUserResponse;
import com.dpccgaming.backend.auth.dto.LoginRequest;
import com.dpccgaming.backend.auth.dto.LoginResponse;
import com.dpccgaming.backend.auth.dto.RegisterRequest;
import com.dpccgaming.backend.auth.dto.RegisterResponse;
import com.dpccgaming.backend.auth.dto.UserProfileResponse;
import com.dpccgaming.backend.auth.dto.VerifyTokenResponse;
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

    public AuthServiceImpl(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
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
        user.setRole("user");
        user.setIsBanned(0);

        int rows = userMapper.insert(user);
        if (rows != 1) {
            throw new BusinessException("REGISTER_FAILED", "注册失败");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        return new RegisterResponse("注册成功", token, toAuthUserResponse(user));
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("AUTH_INVALID", "用户名或密码错误");
        }

        if (Integer.valueOf(1).equals(user.getIsBanned())) {
            throw new BusinessException("USER_BANNED", "账户已被禁用");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        return new LoginResponse("登录成功", token, toAuthUserResponse(user));
    }

    @Override
    public VerifyTokenResponse verifyToken(Long userId, String username) {
        User user = loadUser(userId);
        return new VerifyTokenResponse(true, toAuthUserResponse(user));
    }

    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        return toUserProfileResponse(loadUser(userId));
    }

    @Override
    public CurrentUserResponse getCurrentUser(Long userId) {
        return new CurrentUserResponse(toUserProfileResponse(loadUser(userId)));
    }

    private User loadUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }
        return user;
    }

    private AuthUserResponse toAuthUserResponse(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getIsBanned()
        );
    }

    private UserProfileResponse toUserProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getIsBanned(),
                user.getCreatedAt()
        );
    }
}
