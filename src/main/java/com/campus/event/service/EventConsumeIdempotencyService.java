package com.campus.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventConsumeIdempotencyService {

    private static final String CONSUMED_EVENT_KEY_PREFIX = "event:consumed:";
    private static final Duration CONSUMED_EVENT_TTL = Duration.ofDays(7);

    private final StringRedisTemplate stringRedisTemplate;

    public boolean markIfFirstConsume(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return true;
        }

        try {
            Boolean marked = stringRedisTemplate.opsForValue()
                    .setIfAbsent(CONSUMED_EVENT_KEY_PREFIX + eventId, "1", CONSUMED_EVENT_TTL);
            return Boolean.TRUE.equals(marked);
        } catch (Exception exception) {
            log.warn("event idempotency check failed, eventId={}", eventId, exception);
            return true;
        }
    }
}
