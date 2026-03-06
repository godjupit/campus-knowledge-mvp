package com.campus.optimize.controller;

import com.campus.common.result.ApiResponse;
import com.campus.knowledge.dto.PostSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OptimizeController {

    @GetMapping("/recommend")
    public ApiResponse<List<PostSummaryResponse>> recommend() {
        return ApiResponse.todo("TODO: 调用 OptimizeService 实现简单推荐");
    }

    @GetMapping("/optimize/rate-limit-check")
    public ApiResponse<String> rateLimitCheck() {
        return ApiResponse.todo("TODO: 实现接口限流拦截");
    }
}
