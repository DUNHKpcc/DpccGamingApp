package com.dpccgaming.backend.auth.dto;

import java.time.LocalDateTime;

public class UserProfileResponse {
    private final Long id;
    private final String username;
    private final String role;
    private final String status;
    private final LocalDateTime createdAt;

    public UserProfileResponse(Long id, String username, String role, String status, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
