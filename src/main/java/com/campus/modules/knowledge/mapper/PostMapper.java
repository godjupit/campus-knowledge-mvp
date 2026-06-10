package com.campus.modules.knowledge.mapper;

import com.campus.modules.knowledge.dto.PostDetailResponse;
import com.campus.modules.knowledge.dto.PostSummaryResponse;
import com.campus.modules.knowledge.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {

    void insertPost(Post post);

    List<PostSummaryResponse> selectPosts(@Param("offset") int offset, @Param("limit") int limit);

    List<PostSummaryResponse> searchPosts(@Param("keyword") String keyword,
                                          @Param("offset") int offset,
                                          @Param("limit") int limit);

    List<PostSummaryResponse> selectHotPosts(@Param("limit") int limit);

    List<PostSummaryResponse> selectPostsForRag(@Param("limit") int limit);

    int incrementViewCount(@Param("postId") Long postId, @Param("delta") Long delta);

    Long selectPostOwnerId(@Param("postId") Long postId);

    PostDetailResponse postDetail(Long id);
}
