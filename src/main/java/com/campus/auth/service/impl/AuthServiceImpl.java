package com.campus.auth.service.impl;

import com.campus.auth.dto.LoginRequest;
import com.campus.auth.dto.LoginResponse;
import com.campus.auth.dto.RegisterRequest;
import com.campus.auth.entity.User;
import com.campus.auth.mapper.UserMapper;
import com.campus.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;

    @Override
    public LoginResponse login(LoginRequest request) {
        return null;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        User user = new User();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        log.info("insert user");
        userMapper.insert(user);
    }

    @Override
    public boolean findByName(String username) {
        return userMapper.findByUsername(username) != null;
    }

    @Override
    public boolean findByEmail(String mail) {
        return userMapper.findByEmail(mail) != null;
    }
}
