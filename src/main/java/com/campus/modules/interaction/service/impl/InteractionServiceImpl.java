package com.campus.modules.interaction.service.impl;

import com.campus.common.context.UserContext;
import com.campus.common.exception.BusinessException;
import com.campus.common.exception.ErrorCode;
import com.campus.common.ratelimit.RedisRateLimitService;
import com.campus.infrastructure.event.config.RabbitMqConfig;
import com.campus.infrastructure.event.service.EventOutboxService;
import com.campus.infrastructure.event.support.EventMessageFactory;
import com.campus.modules.interaction.dto.CommentResponse;
import com.campus.modules.interaction.dto.CreateCommentRequest;
import com.campus.modules.interaction.mapper.InteractionMapper;
import com.campus.modules.interaction.service.InteractionService;
import com.campus.modules.search.service.HotPostCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;


@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {
    private static final Duration LIKE_RATE_LIMIT_WINDOW = Duration.ofSeconds(10);
    private static final Duration FAVORITE_RATE_LIMIT_WINDOW = Duration.ofSeconds(10);
    private static final Duration COMMENT_RATE_LIMIT_WINDOW = Duration.ofMinutes(1);
    private static final int LIKE_RATE_LIMIT = 20;
    private static final int FAVORITE_RATE_LIMIT = 20;
    private static final int COMMENT_RATE_LIMIT = 10;

    private final InteractionMapper interactionMapper;
    private final HotPostCacheService hotPostCacheService;
    private final EventOutboxService eventOutboxService;
    private final RedisRateLimitService redisRateLimitService;

    @Override
    @Transactional
    public void comment(CreateCommentRequest request){
        Long userId = UserContext.getUserId();
        redisRateLimitService.checkUserLimit("comment", userId, COMMENT_RATE_LIMIT, COMMENT_RATE_LIMIT_WINDOW);
        Long postId = request.getPostId();
        if(interactionMapper.postIdExists(postId) == 0){
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        interactionMapper.insertComment(userId, postId, request.getContent(), request.getParentId());
        interactionMapper.incrementCommentCount(postId);
        eventOutboxService.saveEvent(
                RabbitMqConfig.COMMENT_CREATED_ROUTING_KEY,
                RabbitMqConfig.COMMENT_CREATED_ROUTING_KEY,
                EventMessageFactory.create(RabbitMqConfig.COMMENT_CREATED_ROUTING_KEY, postId, userId, request.getContent()));
        hotPostCacheService.evict();
    }

    @Override
    public List<CommentResponse> listComments(Long postId) {
        if(interactionMapper.postIdExists(postId) == 0){
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        return interactionMapper.selectCommentsByPostId(postId);
    }

    @Override
    @Transactional
    public void like(Long postId) {
        Long userId = UserContext.getUserId();
        redisRateLimitService.checkUserLimit("like", userId, LIKE_RATE_LIMIT, LIKE_RATE_LIMIT_WINDOW);
        if(interactionMapper.postIdExists(postId) == 0){
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        if(interactionMapper.ifUserLikedPost(userId, postId) > 0){
            return; // 已经点赞过了，幂等处理
        }
        interactionMapper.insertLikeRecord(userId, postId);
        interactionMapper.incrementLikeCount(postId);
        eventOutboxService.saveEvent(
                RabbitMqConfig.LIKE_CREATED_ROUTING_KEY,
                RabbitMqConfig.LIKE_CREATED_ROUTING_KEY,
                EventMessageFactory.create(RabbitMqConfig.LIKE_CREATED_ROUTING_KEY, postId, userId, null));
        hotPostCacheService.evict();

    }

    @Override
    @Transactional
    public void favorite(Long postId) {
        Long userId = UserContext.getUserId();
        redisRateLimitService.checkUserLimit("favorite", userId, FAVORITE_RATE_LIMIT, FAVORITE_RATE_LIMIT_WINDOW);
        if(interactionMapper.postIdExists(postId) == 0){
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        if(interactionMapper.ifUserFavoritedPost(userId, postId) > 0){
            return; // 已经收藏过了，幂等处理
        }
        interactionMapper.insertFavoriteRecord(userId, postId);
        interactionMapper.incrementFavoriteCount(postId);
        hotPostCacheService.evict();

    }


}
