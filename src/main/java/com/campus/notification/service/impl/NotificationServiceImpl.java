package com.campus.notification.service.impl;

import com.campus.common.context.UserContext;
import com.campus.knowledge.mapper.PostMapper;
import com.campus.notification.dto.NotificationResponse;
import com.campus.notification.entity.Notification;
import com.campus.notification.mapper.NotificationMapper;
import com.campus.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;
    private static final String COMMENT_NOTIFICATION_TYPE = "COMMENT_CREATED";

    private final NotificationMapper notificationMapper;
    private final PostMapper postMapper;

    @Override
    public void createCommentNotification(Long postId, Long actorUserId, String contentPreview) {
        Long receiverUserId = postMapper.selectPostOwnerId(postId);
        if (receiverUserId == null) {
            log.warn("skip comment notification because post owner not found, postId={}", postId);
            return;
        }
        if (receiverUserId.equals(actorUserId)) {
            log.info("skip comment notification for self comment, postId={}, actorUserId={}", postId, actorUserId);
            return;
        }

        Notification notification = new Notification();
        notification.setReceiverUserId(receiverUserId);
        notification.setActorUserId(actorUserId);
        notification.setType(COMMENT_NOTIFICATION_TYPE);
        notification.setPostId(postId);
        notification.setContent(buildCommentContent(contentPreview));
        notificationMapper.insert(notification);

        log.info("comment notification created, notificationId={}, receiverUserId={}, actorUserId={}, postId={}",
                notification.getId(),
                receiverUserId,
                actorUserId,
                postId);
    }

    @Override
    public List<NotificationResponse> listMine(Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        int safePage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = (safePage - 1) * safeSize;
        return notificationMapper.selectByReceiver(userId, offset, safeSize);
    }

    private String buildCommentContent(String contentPreview) {
        if (contentPreview == null || contentPreview.isBlank()) {
            return "有人评论了你的帖子";
        }
        return "有人评论了你的帖子：" + contentPreview;
    }
}
