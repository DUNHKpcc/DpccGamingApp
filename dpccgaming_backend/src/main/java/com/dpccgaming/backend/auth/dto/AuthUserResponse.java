package com.dpccgaming.backend.auth.dto;

public class AuthUserResponse {
    private final Long id;
    private final String username;
    private final String role;
    private final Integer isBanned;

    public AuthUserResponse(Long id, String username, String role, Integer isBanned) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.isBanned = isBanned;
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
}
