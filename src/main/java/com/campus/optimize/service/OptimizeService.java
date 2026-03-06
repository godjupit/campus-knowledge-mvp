package com.campus.optimize.service;

import com.campus.knowledge.dto.PostSummaryResponse;

import java.util.List;

public interface OptimizeService {

    // TODO: 实现简单推荐算法

    List<PostSummaryResponse> recommend();

    // TODO: 从 Redis 读取热门帖子缓存

    List<PostSummaryResponse> hotFromCache();
}
