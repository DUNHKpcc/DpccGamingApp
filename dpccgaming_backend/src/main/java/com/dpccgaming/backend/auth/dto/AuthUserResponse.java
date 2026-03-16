package com.dpccgaming.backend.auth.dto;

public class AuthUserResponse {
    private final Long id;
    private final String username;
    private final String role;
    private final String status;

    public AuthUserResponse(Long id, String username, String role, String status) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }
}
