package com.dpccgaming.backend.auth.dto;

import java.time.LocalDateTime;

public class UserProfileResponse {
    private final Long id;
    private final String username;
    private final String role;
    private final Integer isBanned;
    private final LocalDateTime createdAt;

    public UserProfileResponse(Long id, String username, String role, Integer isBanned, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.isBanned = isBanned;
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

    public Integer getIsBanned() {
        return isBanned;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
