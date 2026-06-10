package com.campus.modules.interaction.mapper;

import com.campus.modules.interaction.dto.CommentResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InteractionMapper {

    int postIdExists(@Param("postId") Long postId);

    int incrementLikeCount(@Param("postId") Long postId);

    int incrementFavoriteCount(@Param("postId") Long postId);

    int incrementCommentCount(@Param("postId") Long postId);

    int ifUserLikedPost(@Param("userId") Long userId, @Param("postId") Long postId);

    int ifUserFavoritedPost(@Param("userId") Long userId, @Param("postId") Long postId);

    List<CommentResponse> selectCommentsByPostId(@Param("postId") Long postId);

    void insertComment(@Param("userId") Long userId,
                       @Param("postId") Long postId,
                       @Param("content") String content,
                       @Param("parentId") Long parentId);

    void insertLikeRecord(@Param("userId") Long userId, @Param("postId") Long postId);

    void insertFavoriteRecord(@Param("userId") Long userId, @Param("postId") Long postId);
}
