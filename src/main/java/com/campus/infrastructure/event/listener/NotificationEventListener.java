package com.campus.infrastructure.event.listener;

import com.campus.infrastructure.event.config.RabbitMqConfig;
import com.campus.infrastructure.event.dto.EventMessage;
import com.campus.infrastructure.event.service.EventIdempotencyService;
import com.campus.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventListener {

    private final EventIdempotencyService eventIdempotencyService;
    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMqConfig.COMMENT_CREATED_QUEUE)
    public void onCommentCreated(EventMessage message) {
        if (isDuplicate(message)) {
            return;
        }
        notificationService.createCommentNotification(
                message.getPostId(),
                message.getActorUserId(),
                message.getContentPreview());
        log.info("comment.created consumed, eventId={}, postId={}, actorUserId={}, preview={}",
                message.getEventId(),
                message.getPostId(),
                message.getActorUserId(),
                message.getContentPreview());
    }

    @RabbitListener(queues = RabbitMqConfig.LIKE_CREATED_QUEUE)
    public void onLikeCreated(EventMessage message) {
        if (isDuplicate(message)) {
            return;
        }
        notificationService.createLikeNotification(
                message.getPostId(),
                message.getActorUserId());
        log.info("like.created consumed, eventId={}, postId={}, actorUserId={}",
                message.getEventId(),
                message.getPostId(),
                message.getActorUserId());
    }

    @RabbitListener(queues = RabbitMqConfig.POST_CREATED_QUEUE)
    public void onPostCreated(EventMessage message) {
        if (isDuplicate(message)) {
            return;
        }
        log.info("post.created consumed, eventId={}, postId={}, actorUserId={}, preview={}",
                message.getEventId(),
                message.getPostId(),
                message.getActorUserId(),
                message.getContentPreview());
    }

    private boolean isDuplicate(EventMessage message) {
        boolean firstConsume = eventIdempotencyService.markIfFirstConsume(message.getEventId());
        if (!firstConsume) {
            log.info("event skipped because already consumed, eventId={}, eventType={}",
                    message.getEventId(),
                    message.getEventType());
            return true;
        }
        return false;
    }
}
