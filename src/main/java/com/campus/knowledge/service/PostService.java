package com.campus.knowledge.service;

import com.campus.knowledge.dto.CreatePostRequest;
import com.campus.knowledge.dto.PostDetailResponse;
import com.campus.knowledge.dto.PostSummaryResponse;

import java.util.List;

public interface PostService {

    // TODO: 实现帖子分页查询

    List<PostSummaryResponse> list(Integer page, Integer size);

    // TODO: 实现帖子详情查询

    PostDetailResponse detail(Long id);

    // TODO: 实现帖子发布

    void create(CreatePostRequest request);
}
