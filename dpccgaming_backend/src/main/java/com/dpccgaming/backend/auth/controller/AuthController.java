package com.dpccgaming.backend.auth.controller;


import com.dpccgaming.backend.auth.dto.ReigisterRequest;
import com.dpccgaming.backend.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    @PostMapping("/register")
    public ApiResponse<String> register(
            @Valid
            @RequestBody ReigisterRequest request
    ) {
        return ApiResponse.ok("register endpoint ok");
    }
}
