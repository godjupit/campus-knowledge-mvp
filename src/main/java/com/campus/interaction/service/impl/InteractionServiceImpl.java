package com.campus.interaction.service.impl;

import com.campus.common.context.UserContext;
import com.campus.common.exception.BusinessException;
import com.campus.common.exception.ErrorCode;
import com.campus.interaction.dto.CreateCommentRequest;
import com.campus.interaction.mapper.InteractionMapper;
import com.campus.interaction.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {
    private final InteractionMapper interactionMapper;
    @Override
    public void comment(CreateCommentRequest request){

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


    }


}
