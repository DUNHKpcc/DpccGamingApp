package com.dpccgaming.backend.auth.controller;


import com.dpccgaming.backend.auth.dto.ReigisterRequest;
import com.dpccgaming.backend.auth.entity.User;
import com.dpccgaming.backend.auth.service.AuthService;
import com.dpccgaming.backend.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ReigisterResponse register(@Valid @RequestBody RegisterRequest request){
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request){
        return authService.login(request);
    }

    @GetMapping("/verify-token")
    public VerifyTokenResponse verifyToken(Authentication authentication){
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return authService.verifyToken(loginUser.userId(),loginUser.username());
    }

    @GetMapping("/user/profile")
    public UserProfileResponse getUserProfile(Authentication authentication){
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return authService.getUserProfile(loginUser.userId());
    }

    @GetMapping("/auth/me")
    public CurrentUserResponse getCurrentUser(Authentication authentication){
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return authService.getCurrentUser(loginUser.userId());
    }

    @GetMapping("/me")
    public CurrentUserResponse getCurrentUserAlias(Authentication authentication){
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return authService.getCurrentUser(LoginUser.userId());
    }





}
