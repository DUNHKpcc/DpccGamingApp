package com.dpccgaming.backend.auth.controller;

import com.dpccgaming.backend.auth.dto.CurrentUserResponse;
import com.dpccgaming.backend.auth.dto.LoginRequest;
import com.dpccgaming.backend.auth.dto.LoginResponse;
import com.dpccgaming.backend.auth.dto.RegisterRequest;
import com.dpccgaming.backend.auth.dto.RegisterResponse;
import com.dpccgaming.backend.auth.dto.UserProfileResponse;
import com.dpccgaming.backend.auth.dto.VerifyTokenResponse;
import com.dpccgaming.backend.auth.security.LoginUser;
import com.dpccgaming.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/verify-token")
    public VerifyTokenResponse verifyToken(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return authService.verifyToken(loginUser.userId(), loginUser.username());
    }

    @GetMapping("/user/profile")
    public UserProfileResponse getUserProfile(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return authService.getUserProfile(loginUser.userId());
    }

    @GetMapping("/auth/me")
    public CurrentUserResponse getCurrentUser(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return authService.getCurrentUser(loginUser.userId());
    }

    @GetMapping("/me")
    public CurrentUserResponse getCurrentUserAlias(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return authService.getCurrentUser(loginUser.userId());
    }
}
