package com.campus.interaction.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InteractionMapper {
	// TODO: 定义评论、点赞、收藏相关 SQL 映射方法

    int postIdExists(Long postId);
    int incrementLikeCount(Long postId);
    int incrementFavoriteCount(Long postId);

    int ifUserLikedPost(Long userId, Long postId);
    int ifUserFavoritedPost(Long userId, Long postId);

    void insertLikeRecord(Long userId, Long postId);
    void insertFavoriteRecord(Long userId, Long postId);


}
