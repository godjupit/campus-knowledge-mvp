package com.campus.auth.controller;

import com.campus.auth.dto.UserInfoResponse;
import com.campus.auth.entity.User;
import com.campus.auth.service.UserService;
import com.campus.common.context.UserContext;
import com.campus.common.result.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getCurrentUser() {
        Long id = UserContext.getUserId();
        if (id == null) {
            return ApiResponse.fail("未认证，请先登录");
        }
        UserInfoResponse response = userService.getCurrentUser();
        return ApiResponse.ok(response);
    }
}
