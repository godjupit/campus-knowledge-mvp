package com.campus.auth.controller;

import com.campus.auth.dto.LoginRequest;
import com.campus.auth.dto.LoginResponse;
import com.campus.auth.dto.RegisterRequest;
import com.campus.auth.service.AuthService;
import com.campus.common.exception.BusinessException;
import com.campus.common.exception.ErrorCode;
import com.campus.common.result.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        if(authService.findByName(request.getUsername())){
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }else if(authService.findByEmail(request.getEmail())){
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }
        authService.register(request);
        return ApiResponse.ok(null);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }
}
