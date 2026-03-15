package com.campus.knowledge.service;

import com.campus.knowledge.dto.CreatePostRequest;
import com.campus.knowledge.dto.PostDetailResponse;
import com.campus.knowledge.dto.PostSummaryResponse;

import java.util.List;

public interface PostService {

    List<PostSummaryResponse> list(Integer page, Integer size);

    PostDetailResponse detail(Long id);

    void create(CreatePostRequest request);
}
