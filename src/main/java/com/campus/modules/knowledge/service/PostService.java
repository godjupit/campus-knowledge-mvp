package com.campus.modules.knowledge.service;

import com.campus.modules.knowledge.dto.CreatePostRequest;
import com.campus.modules.knowledge.dto.PostDetailResponse;
import com.campus.modules.knowledge.dto.PostSummaryResponse;

import java.util.List;

public interface PostService {

    List<PostSummaryResponse> list(Integer page, Integer size);

    PostDetailResponse detail(Long id);

    void create(CreatePostRequest request);
}
