package com.dpccgaming.backend.auth.service;

import com.dpccgaming.backend.auth.dto.CurrentUserResponse;
import com.dpccgaming.backend.auth.dto.LoginRequest;
import com.dpccgaming.backend.auth.dto.LoginResponse;
import com.dpccgaming.backend.auth.dto.RegisterRequest;
import com.dpccgaming.backend.auth.dto.RegisterResponse;
import com.dpccgaming.backend.auth.dto.UserProfileResponse;
import com.dpccgaming.backend.auth.dto.VerifyTokenResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    VerifyTokenResponse verifyToken(Long userId, String username);

    UserProfileResponse getUserProfile(Long userId);

    CurrentUserResponse getCurrentUser(Long userId);
}
