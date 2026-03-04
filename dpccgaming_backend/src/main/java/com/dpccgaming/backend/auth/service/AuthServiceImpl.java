package com.dpccgaming.backend.auth.service;

import com.dpccgaming.backend.auth.dto.ReigisterRequest;
import com.dpccgaming.backend.auth.repository.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public void register(ReigisterRequest request) {

    }
}
