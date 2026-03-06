package com.campus.knowledge.controller;

import com.campus.common.result.ApiResponse;
import com.campus.knowledge.dto.CreatePostRequest;
import com.campus.knowledge.dto.PostDetailResponse;
import com.campus.knowledge.dto.PostSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CreatePostRequest request) {
        return ApiResponse.todo("TODO: 调用 PostService 完成发布帖子");
    }

    @GetMapping
    public ApiResponse<List<PostSummaryResponse>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ApiResponse.ok(List.of(new PostSummaryResponse(1L, "示例帖子", "springboot,mybatis")));
    }

    @GetMapping("/{id}")
    public ApiResponse<PostDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(new PostDetailResponse(id, "示例标题", "示例内容", "springboot"));
    }
}
