package com.campus.interaction.service;

import com.campus.interaction.dto.CommentResponse;
import com.campus.interaction.dto.CreateCommentRequest;

import java.util.List;

public interface InteractionService {

    void comment(CreateCommentRequest request);

    List<CommentResponse> listComments(Long postId);

    void like(Long postId);

    void favorite(Long postId);
}
