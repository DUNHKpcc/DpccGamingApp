package com.dpccgaming.backend.auth.dto;

public class CurrentUserResponse {
    private final UserProfileResponse user;

    public CurrentUserResponse(UserProfileResponse user) {
        this.user = user;
    }

    public UserProfileResponse getUser() {
        return user;
    }
}
