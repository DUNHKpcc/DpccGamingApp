package com.dpccgaming.backend.auth.dto;

public class VerifyTokenResponse {
    private final boolean valid;
    private final AuthUserResponse user;

    public VerifyTokenResponse(boolean valid, AuthUserResponse user) {
        this.valid = valid;
        this.user = user;
    }

    public boolean isValid() {
        return valid;
    }

    public AuthUserResponse getUser() {
        return user;
    }
}
