package com.campus.modules.search.service.impl;

import com.campus.modules.knowledge.dto.PostSummaryResponse;
import com.campus.modules.knowledge.mapper.PostMapper;
import com.campus.modules.search.service.HotPostCacheService;
import com.campus.modules.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;
    private static final int HOT_LIMIT = 10;

    private final PostMapper postMapper;
    private final HotPostCacheService hotPostCacheService;

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
        List<PostSummaryResponse> cachedPosts = hotPostCacheService.read();
        if (cachedPosts != null) {
            logHotPostsResult("redis", cachedPosts.size(), startNanos);
            return cachedPosts;
        }

        String lockValue = UUID.randomUUID().toString();
        if (hotPostCacheService.tryLock(lockValue)) {
            try {
                List<PostSummaryResponse> doubleCheckedPosts = hotPostCacheService.read();
                if (doubleCheckedPosts != null) {
                    logHotPostsResult("redis_after_lock", doubleCheckedPosts.size(), startNanos);
                    return doubleCheckedPosts;
                }

                List<PostSummaryResponse> hotPosts = postMapper.selectHotPosts(HOT_LIMIT);
                hotPostCacheService.write(hotPosts);
                logHotPostsResult("database_with_lock", hotPosts.size(), startNanos);
                return hotPosts;
            } finally {
                hotPostCacheService.unlock(lockValue);
            }
        }

        List<PostSummaryResponse> retryPosts = hotPostCacheService.readAfterShortWait();
        if (retryPosts != null) {
            logHotPostsResult("redis_after_wait", retryPosts.size(), startNanos);
            return retryPosts;
        }

        List<PostSummaryResponse> fallbackPosts = postMapper.selectHotPosts(HOT_LIMIT);
        logHotPostsResult("database_fallback_no_lock", fallbackPosts.size(), startNanos);
        return fallbackPosts;
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
