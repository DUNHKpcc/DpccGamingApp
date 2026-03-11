package com.dpccgaming.backend.auth.dto;

public class RegisterResponse {
    private final String message;
    private final String token;
    private final AuthUserResponse user;

    public RegisterResponse(String message, String token, AuthUserResponse user) {
        this.message = message;
        this.token = token;
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public AuthUserResponse getUser() {
        return user;
    }
}
