package com.campus.search.service.impl;

import com.campus.knowledge.dto.PostSummaryResponse;
import com.campus.knowledge.mapper.PostMapper;
import com.campus.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;
    private static final int HOT_LIMIT = 10;

    private final PostMapper postMapper;

    @Override
    public List<PostSummaryResponse> search(String keyword, Integer page, Integer size) {
        // TODO: keyword 为空时怎么处理？可以抛异常，也可以返回空列表
        if(keyword == null) {
            return List.of();
        }
        // TODO: page 小于 1 时修正为 1
        int safePage = page == null || page < 1 ? DEFAULT_PAGE : page;
        // TODO: size 小于 1 时用默认值 10，最大不要超过 50
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        // TODO: 根据 page 和 size 计算 offset
        int offset = (safePage - 1) * safeSize;
        // TODO: 调用 postMapper.searchPosts(keyword, offset, size)
        return postMapper.searchPosts(keyword, offset, safeSize);
    }

    @Override
    public List<PostSummaryResponse> hot() {
        // TODO: 调用热门帖子查询
        // TODO: 后续决定热门排序规则，例如浏览数、点赞数、收藏数、评论数的综合分
        return postMapper.selectHotPosts(HOT_LIMIT);
    }
}
