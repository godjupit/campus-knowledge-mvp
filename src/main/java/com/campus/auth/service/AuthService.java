package com.campus.auth.service;

import com.campus.auth.dto.LoginRequest;
import com.campus.auth.dto.LoginResponse;
import com.campus.auth.dto.RegisterRequest;

public interface AuthService {

    // TODO: 实现登录校验、密码比对、JWT 生成

    LoginResponse login(LoginRequest request);

    // TODO: 实现注册校验、密码加密、用户写入

    void register(RegisterRequest request);

    boolean findByName(String username);

    boolean findByEmail(String mail);
}
