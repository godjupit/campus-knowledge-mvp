package com.campus.interaction.controller;

import com.campus.common.result.ApiResponse;
import com.campus.interaction.dto.CreateCommentRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InteractionController {

    @PostMapping("/comments")
    public ApiResponse<Void> comment(@Valid @RequestBody CreateCommentRequest request) {
        return ApiResponse.todo("TODO: 调用 InteractionService 完成评论功能");
    }

    @PostMapping("/posts/{id}/like")
    public ApiResponse<Void> like(@PathVariable Long id) {
        return ApiResponse.todo("TODO: 调用 InteractionService 完成点赞功能");
    }

    @PostMapping("/posts/{id}/favorite")
    public ApiResponse<Void> favorite(@PathVariable Long id) {
        return ApiResponse.todo("TODO: 调用 InteractionService 完成收藏功能");
    }
}
