package com.dpccgaming.backend.auth.service;

import com.dpccgaming.backend.auth.dto.ReigisterRequest;

public interface AuthService {
    void register(ReigisterRequest request);
}
