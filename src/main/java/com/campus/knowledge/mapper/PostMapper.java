package com.campus.knowledge.mapper;

import com.campus.knowledge.dto.PostDetailResponse;
import com.campus.knowledge.dto.PostSummaryResponse;
import com.campus.knowledge.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {

    void insertPost(Post post);

    List<PostSummaryResponse> selectPosts(@Param("offset") int offset, @Param("limit") int limit);

    PostDetailResponse postDetail(Long id);
}
