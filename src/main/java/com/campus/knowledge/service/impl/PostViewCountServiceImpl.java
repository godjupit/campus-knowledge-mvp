package com.campus.knowledge.service.impl;

import com.campus.knowledge.mapper.PostMapper;
import com.campus.knowledge.service.PostViewCountService;
import com.campus.search.service.HotPostsCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostViewCountServiceImpl implements PostViewCountService {

    private static final String VIEW_COUNT_KEY_PREFIX = "post:view:";
    private static final String VIEW_COUNT_DIRTY_SET_KEY = "post:view:dirty";

    private final StringRedisTemplate stringRedisTemplate;
    private final PostMapper postMapper;
    private final HotPostsCacheService hotPostsCacheService;

    @Override
    public long recordView(Long postId) {
        if (postId == null) {
            return 0;
        }

        try {
            Long pendingViews = stringRedisTemplate.opsForValue().increment(viewCountKey(postId));
            stringRedisTemplate.opsForSet().add(VIEW_COUNT_DIRTY_SET_KEY, String.valueOf(postId));
            return pendingViews == null ? 0 : pendingViews;
        } catch (Exception exception) {
            log.warn("record post view failed, postId={}", postId, exception);
            return 0;
        }
    }

    @Scheduled(fixedDelay = 30000)
    public void syncViewCountsToDatabase() {
        Set<String> postIds = dirtyPostIds();
        if (postIds == null || postIds.isEmpty()) {
            return;
        }

        int syncedPosts = 0;
        long syncedViews = 0;
        long startNanos = System.nanoTime();

        for (String postIdValue : postIds) {
            Long postId = parsePostId(postIdValue);
            if (postId == null) {
                stringRedisTemplate.opsForSet().remove(VIEW_COUNT_DIRTY_SET_KEY, postIdValue);
                continue;
            }

            String key = viewCountKey(postId);
            Long delta = getAndDeleteViewDelta(key);
            if (delta == null || delta <= 0) {
                stringRedisTemplate.opsForSet().remove(VIEW_COUNT_DIRTY_SET_KEY, postIdValue);
                continue;
            }

            try {
                int updatedRows = postMapper.incrementViewCount(postId, delta);
                if (updatedRows > 0) {
                    syncedPosts++;
                    syncedViews += delta;
                    stringRedisTemplate.opsForSet().remove(VIEW_COUNT_DIRTY_SET_KEY, postIdValue);
                } else {
                    restoreViewDelta(key, delta);
                }
            } catch (Exception exception) {
                restoreViewDelta(key, delta);
                log.warn("sync post view count failed, postId={}, delta={}", postId, delta, exception);
            }
        }

        if (syncedPosts > 0) {
            hotPostsCacheService.evict();
            log.info("post view counts synced, posts={}, views={}, cost={}ms",
                    syncedPosts,
                    syncedViews,
                    elapsedMillis(startNanos));
        }
    }

    private Set<String> dirtyPostIds() {
        try {
            return stringRedisTemplate.opsForSet().members(VIEW_COUNT_DIRTY_SET_KEY);
        } catch (Exception exception) {
            log.warn("read dirty post view ids failed", exception);
            return Set.of();
        }
    }

    private Long getAndDeleteViewDelta(String key) {
        try {
            String value = stringRedisTemplate.opsForValue().getAndDelete(key);
            if (value == null || value.isBlank()) {
                return null;
            }
            return Long.parseLong(value);
        } catch (Exception exception) {
            log.warn("read post view delta failed, key={}", key, exception);
            return null;
        }
    }

    private void restoreViewDelta(String key, Long delta) {
        try {
            stringRedisTemplate.opsForValue().increment(key, delta);
        } catch (Exception exception) {
            log.warn("restore post view delta failed, key={}, delta={}", key, delta, exception);
        }
    }

    private Long parsePostId(String postIdValue) {
        if (postIdValue == null || postIdValue.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(postIdValue);
        } catch (NumberFormatException exception) {
            log.warn("invalid dirty post id={}", postIdValue);
            return null;
        }
    }

    private String viewCountKey(Long postId) {
        return VIEW_COUNT_KEY_PREFIX + postId;
    }

    private double elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000.0;
    }
}
