package com.campus.event.listener;

import com.campus.event.config.RabbitMqConfig;
import com.campus.event.dto.EventMessage;
import com.campus.event.service.EventConsumeIdempotencyService;
import com.campus.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CampusEventListener {

    private final EventConsumeIdempotencyService eventConsumeIdempotencyService;
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
        boolean firstConsume = eventConsumeIdempotencyService.markIfFirstConsume(message.getEventId());
        if (!firstConsume) {
            log.info("event skipped because already consumed, eventId={}, eventType={}",
                    message.getEventId(),
                    message.getEventType());
            return true;
        }
        return false;
    }
}
