package com.campus.common.ratelimit;

import com.campus.common.exception.BusinessException;
import com.campus.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisRateLimitService {

    private final StringRedisTemplate stringRedisTemplate;

    public void checkUserLimit(String action, Long userId, int limit, Duration window) {
        if (userId == null) {
            return;
        }
        checkLimit("rate:" + action + ":user:" + userId, limit, window);
    }

    public void checkIpLimit(String action, int limit, Duration window) {
        checkLimit("rate:" + action + ":ip:" + clientIp(), limit, window);
    }

    private void checkLimit(String key, int limit, Duration window) {
        try {
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                stringRedisTemplate.expire(key, window);
            }
            if (count != null && count > limit) {
                log.warn("rate limit exceeded, key={}, count={}, limit={}, windowSeconds={}",
                        key,
                        count,
                        limit,
                        window.toSeconds());
                throw new BusinessException(ErrorCode.RATE_LIMITED);
            }
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("rate limit check failed, key={}", key, exception);
        }
    }

    private String clientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }

        HttpServletRequest request = attributes.getRequest();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}
