package com.campus.interaction.service;

import com.campus.interaction.dto.CreateCommentRequest;

public interface InteractionService {

    // TODO: 实现评论创建

    void comment(CreateCommentRequest request);

    // TODO: 实现点赞（幂等）

    void like(Long postId);

    // TODO: 实现收藏（幂等）

    void favorite(Long postId);
}
