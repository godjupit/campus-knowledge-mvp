package com.campus.event.support;

import com.campus.event.dto.EventMessage;

import java.time.LocalDateTime;
import java.util.UUID;

public final class EventMessageFactory {

    private static final int CONTENT_PREVIEW_LENGTH = 80;

    private EventMessageFactory() {
    }

    public static EventMessage create(String eventType, Long postId, Long actorUserId, String content) {
        return new EventMessage(
                UUID.randomUUID().toString(),
                eventType,
                postId,
                actorUserId,
                LocalDateTime.now(),
                preview(content)
        );
    }

    private static String preview(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        String trimmed = content.trim();
        return trimmed.length() <= CONTENT_PREVIEW_LENGTH
                ? trimmed
                : trimmed.substring(0, CONTENT_PREVIEW_LENGTH);
    }
}
