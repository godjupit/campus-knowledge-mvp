package com.campus.modules.search.service;

import com.campus.modules.knowledge.dto.PostSummaryResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotPostCacheService {

    private static final String HOT_POSTS_CACHE_KEY = "hot:posts:top10";
    private static final String HOT_POSTS_LOCK_KEY = "hot:posts:top10:lock";
    private static final Duration HOT_POSTS_CACHE_TTL = Duration.ofSeconds(60);
    private static final Duration HOT_POSTS_LOCK_TTL = Duration.ofSeconds(5);
    private static final Duration HOT_POSTS_LOCK_WAIT = Duration.ofMillis(50);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public List<PostSummaryResponse> read() {
        long startNanos = System.nanoTime();
        try {
            String cachedJson = stringRedisTemplate.opsForValue().get(HOT_POSTS_CACHE_KEY);
            if (cachedJson == null || cachedJson.isBlank()) {
                log.debug("hot posts cache miss, key={}, cost={}ms",
                        HOT_POSTS_CACHE_KEY,
                        elapsedMillis(startNanos));
                return null;
            }

            List<PostSummaryResponse> cachedPosts = objectMapper.readValue(cachedJson, new TypeReference<List<PostSummaryResponse>>() {
            });
            log.debug("hot posts cache hit, key={}, size={}, cost={}ms",
                    HOT_POSTS_CACHE_KEY,
                    cachedPosts.size(),
                    elapsedMillis(startNanos));
            return cachedPosts;
        } catch (Exception ignored) {
            log.warn("hot posts cache read failed, key={}, cost={}ms",
                    HOT_POSTS_CACHE_KEY,
                    elapsedMillis(startNanos),
                    ignored);
            return null;
        }
    }

    public void write(List<PostSummaryResponse> hotPosts) {
        long startNanos = System.nanoTime();
        try {
            String json = objectMapper.writeValueAsString(hotPosts);
            stringRedisTemplate.opsForValue().set(HOT_POSTS_CACHE_KEY, json, HOT_POSTS_CACHE_TTL);
            log.debug("hot posts cache write success, key={}, size={}, ttlSeconds={}, cost={}ms",
                    HOT_POSTS_CACHE_KEY,
                    hotPosts.size(),
                    HOT_POSTS_CACHE_TTL.toSeconds(),
                    elapsedMillis(startNanos));
        } catch (Exception ignored) {
            log.warn("hot posts cache write failed, key={}, size={}, cost={}ms",
                    HOT_POSTS_CACHE_KEY,
                    hotPosts.size(),
                    elapsedMillis(startNanos),
                    ignored);
        }
    }

    public void evict() {
        long startNanos = System.nanoTime();
        try {
            Boolean deleted = stringRedisTemplate.delete(HOT_POSTS_CACHE_KEY);
            log.info("hot posts cache evicted, key={}, deleted={}, cost={}ms",
                    HOT_POSTS_CACHE_KEY,
                    deleted,
                    elapsedMillis(startNanos));
        } catch (Exception ignored) {
            log.warn("hot posts cache evict failed, key={}, cost={}ms",
                    HOT_POSTS_CACHE_KEY,
                    elapsedMillis(startNanos),
                    ignored);
        }
    }

    public boolean tryLock(String lockValue) {
        long startNanos = System.nanoTime();
        try {
            Boolean locked = stringRedisTemplate.opsForValue()
                    .setIfAbsent(HOT_POSTS_LOCK_KEY, lockValue, HOT_POSTS_LOCK_TTL);
            boolean success = Boolean.TRUE.equals(locked);
            log.debug("hot posts cache rebuild lock {}, key={}, cost={}ms",
                    success ? "acquired" : "not_acquired",
                    HOT_POSTS_LOCK_KEY,
                    elapsedMillis(startNanos));
            return success;
        } catch (Exception exception) {
            log.warn("hot posts cache rebuild lock failed, key={}, cost={}ms",
                    HOT_POSTS_LOCK_KEY,
                    elapsedMillis(startNanos),
                    exception);
            return false;
        }
    }

    public void unlock(String lockValue) {
        long startNanos = System.nanoTime();
        try {
            String currentValue = stringRedisTemplate.opsForValue().get(HOT_POSTS_LOCK_KEY);
            if (!lockValue.equals(currentValue)) {
                log.debug("skip hot posts cache rebuild unlock, key={}, ownerChanged=true, cost={}ms",
                        HOT_POSTS_LOCK_KEY,
                        elapsedMillis(startNanos));
                return;
            }
            Boolean deleted = stringRedisTemplate.delete(HOT_POSTS_LOCK_KEY);
            log.debug("hot posts cache rebuild lock released, key={}, deleted={}, cost={}ms",
                    HOT_POSTS_LOCK_KEY,
                    deleted,
                    elapsedMillis(startNanos));
        } catch (Exception exception) {
            log.warn("hot posts cache rebuild unlock failed, key={}, cost={}ms",
                    HOT_POSTS_LOCK_KEY,
                    elapsedMillis(startNanos),
                    exception);
        }
    }

    public List<PostSummaryResponse> readAfterShortWait() {
        try {
            Thread.sleep(HOT_POSTS_LOCK_WAIT.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        return read();
    }

    private double elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000.0;
    }
}
