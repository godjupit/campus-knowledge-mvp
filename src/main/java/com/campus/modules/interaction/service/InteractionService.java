package com.campus.modules.interaction.service;

import com.campus.modules.interaction.dto.CommentResponse;
import com.campus.modules.interaction.dto.CreateCommentRequest;

import java.util.List;

public interface InteractionService {

    void comment(CreateCommentRequest request);

    List<CommentResponse> listComments(Long postId);

    void like(Long postId);

    void favorite(Long postId);
}
