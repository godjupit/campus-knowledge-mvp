package com.campus.interaction.service.impl;

import com.campus.common.context.UserContext;
import com.campus.common.exception.BusinessException;
import com.campus.common.exception.ErrorCode;
import com.campus.event.config.RabbitMqConfig;
import com.campus.event.service.EventOutboxService;
import com.campus.event.support.EventMessageFactory;
import com.campus.interaction.dto.CommentResponse;
import com.campus.interaction.dto.CreateCommentRequest;
import com.campus.interaction.mapper.InteractionMapper;
import com.campus.interaction.service.InteractionService;
import com.campus.search.service.HotPostsCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {
    private final InteractionMapper interactionMapper;
    private final HotPostsCacheService hotPostsCacheService;
    private final EventOutboxService eventOutboxService;

    @Override
    @Transactional
    public void comment(CreateCommentRequest request){
        Long userId = UserContext.getUserId();
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
        hotPostsCacheService.evict();
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
        hotPostsCacheService.evict();

    }

    @Override
    @Transactional
    public void favorite(Long postId) {
        Long userId = UserContext.getUserId();
        if(interactionMapper.postIdExists(postId) == 0){
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        if(interactionMapper.ifUserFavoritedPost(userId, postId) > 0){
            return; // 已经收藏过了，幂等处理
        }
        interactionMapper.insertFavoriteRecord(userId, postId);
        interactionMapper.incrementFavoriteCount(postId);
        hotPostsCacheService.evict();

    }


}
