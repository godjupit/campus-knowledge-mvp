package com.campus.modules.interaction.controller;

import com.campus.common.result.ApiResponse;
import com.campus.modules.interaction.dto.CommentResponse;
import com.campus.modules.interaction.dto.CreateCommentRequest;
import com.campus.modules.interaction.service.InteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InteractionController {
    private final InteractionService interactionService;
    @PostMapping("/comments")
    public ApiResponse<Void> comment(@Valid @RequestBody CreateCommentRequest request) {
        interactionService.comment(request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<List<CommentResponse>> comments(@PathVariable Long postId) {
        return ApiResponse.ok(interactionService.listComments(postId));
    }

    @PostMapping("/posts/{postId}/like")
    public ApiResponse<Void> like(@PathVariable Long postId) {
        interactionService.like(postId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/posts/{postId}/favorite")
    public ApiResponse<Void> favorite(@PathVariable Long postId) {
        interactionService.favorite(postId);
        return ApiResponse.ok(null);
    }
}
