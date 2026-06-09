package com.campus.interaction.controller;

import com.campus.common.result.ApiResponse;
import com.campus.interaction.dto.CreateCommentRequest;
import com.campus.interaction.service.InteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InteractionController {
    private final InteractionService interactionService;
    @PostMapping("/comments")
    public ApiResponse<Void> comment(@Valid @RequestBody CreateCommentRequest request) {


        return null;
    }

    @PostMapping("/posts/{postid}/like")
    public ApiResponse<Void> like(@PathVariable Long postid) {
        interactionService.like(postid);
        return ApiResponse.ok(null);
    }

    @PostMapping("/posts/{postid}/favorite")
    public ApiResponse<Void> favorite(@PathVariable Long postid) {
        interactionService.favorite(postid);
        return ApiResponse.ok(null);
    }
}
