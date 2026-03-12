package com.campus.knowledge.mapper;

import com.campus.knowledge.entity.Post;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper {
	// TODO: 定义帖子相关 SQL 映射方法

    void insertPost(Post post);
}
