package com.campus.search.service.impl;

import com.campus.knowledge.dto.PostSummaryResponse;
import com.campus.knowledge.mapper.PostMapper;
import com.campus.search.service.HotPostsCacheService;
import com.campus.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;
    private static final int HOT_LIMIT = 10;

    private final PostMapper postMapper;
    private final HotPostsCacheService hotPostsCacheService;

    @Override
    public List<PostSummaryResponse> search(String keyword, Integer page, Integer size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        String trimmedKeyword = keyword.trim();
        int safePage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = (safePage - 1) * safeSize;
        return postMapper.searchPosts(trimmedKeyword, offset, safeSize);
    }

    @Override
    public List<PostSummaryResponse> hot() {
        long startNanos = System.nanoTime();
        List<PostSummaryResponse> cachedPosts = hotPostsCacheService.read();
        if (cachedPosts != null) {
            logHotPostsResult("redis", cachedPosts.size(), startNanos);
            return cachedPosts;
        }

        List<PostSummaryResponse> hotPosts = postMapper.selectHotPosts(HOT_LIMIT);
        hotPostsCacheService.write(hotPosts);
        logHotPostsResult("database", hotPosts.size(), startNanos);
        return hotPosts;
    }

    private void logHotPostsResult(String source, int size, long startNanos) {
        log.info("hot posts loaded, source={}, size={}, cost={}ms",
                source,
                size,
                elapsedMillis(startNanos));
    }

    private double elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000.0;
    }
}
