package com.campus.knowledge.mapper;

import com.campus.knowledge.dto.PostDetailResponse;
import com.campus.knowledge.dto.PostSummaryResponse;
import com.campus.knowledge.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {
	// TODO: 定义帖子相关 SQL 映射方法

    void insertPost(Post post);


    // 实现分页查询
    List<PostDetailResponse> selectPosts(@Param("offset") int offset, @Param("limit") int limit);

    PostDetailResponse postDetail(Long id);
}
