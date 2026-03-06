package com.campus.search.service;

import com.campus.knowledge.dto.PostSummaryResponse;

import java.util.List;

public interface SearchService {

    // TODO: 实现关键词搜索

    List<PostSummaryResponse> search(String keyword);

    // TODO: 实现热门内容计算

    List<PostSummaryResponse> hot();
}
