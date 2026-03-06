package com.campus.auth.controller;

import com.campus.auth.dto.UserInfoResponse;
import com.campus.common.result.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> me() {
        return ApiResponse.ok(new UserInfoResponse(1L, "demo_user", "demo@campus.com"));
    }
}
