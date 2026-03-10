package com.dpccgaming.backend.auth.service;

import com.dpccgaming.backend.auth.dto.ReigisterRequest;

public interface AuthService {

    RegisterResponse register(ReigisterRequest request);

    LoginResponse login(LogingRequest request);

    VerifyTokenResponse verifyToken(Long userId,String username);
    
    UserProfileResponse getUserProfile(Long userId);

    CurrentUserResponse getCurrentUser(Long userId);

}
