package com.campus.knowledge.service.impl;

import com.campus.common.context.UserContext;
import com.campus.common.exception.BusinessException;
import com.campus.common.exception.ErrorCode;
import com.campus.event.config.RabbitMqConfig;
import com.campus.event.service.EventOutboxService;
import com.campus.event.support.EventMessageFactory;
import com.campus.knowledge.dto.CreatePostRequest;
import com.campus.knowledge.dto.PostDetailResponse;
import com.campus.knowledge.dto.PostSummaryResponse;
import com.campus.knowledge.entity.Post;
import com.campus.knowledge.mapper.PostMapper;
import com.campus.knowledge.service.PostViewCountService;
import com.campus.knowledge.service.PostService;
import com.campus.search.service.HotPostsCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    private final PostMapper postMapper;
    private final HotPostsCacheService hotPostsCacheService;
    private final PostViewCountService postViewCountService;
    private final EventOutboxService eventOutboxService;

    @Override
    public List<PostSummaryResponse> list(Integer page, Integer size) {
        int safePage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = (safePage - 1) * safeSize;
        return postMapper.selectPosts(offset, safeSize);
    }

    @Override
    public PostDetailResponse detail(Long id) {
        PostDetailResponse response = postMapper.postDetail(id);
        if (response == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        long pendingViews = postViewCountService.recordView(id);
        response.setViewCount(response.getViewCount() + Math.toIntExact(pendingViews));
        return response;
    }

    @Override
    @Transactional
    public void create(CreatePostRequest request) {
        Long userId = UserContext.getUserId();
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setTags(request.getTags());
        postMapper.insertPost(post);
        eventOutboxService.saveEvent(
                RabbitMqConfig.POST_CREATED_ROUTING_KEY,
                RabbitMqConfig.POST_CREATED_ROUTING_KEY,
                EventMessageFactory.create(RabbitMqConfig.POST_CREATED_ROUTING_KEY, post.getId(), userId, request.getTitle()));
        hotPostsCacheService.evict();
    }
}
