package com.campus.knowledge.service.impl;


import com.campus.common.context.UserContext;
import com.campus.knowledge.dto.CreatePostRequest;
import com.campus.knowledge.dto.PostDetailResponse;
import com.campus.knowledge.dto.PostSummaryResponse;
import com.campus.knowledge.entity.Post;
import com.campus.knowledge.mapper.PostMapper;
import com.campus.knowledge.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostMapper postMapper;


    @Override
    public List<PostSummaryResponse> list(Integer page, Integer size) {
        return List.of();
    }

    @Override
    public PostDetailResponse detail(Long id) {
        return null;
    }

    @Override
    public void create(CreatePostRequest request) {
        Post post = new Post();
        post.setUserId(UserContext.getUserId());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setTags(request.getTags());
        postMapper.insertPost(post);
    }
}
