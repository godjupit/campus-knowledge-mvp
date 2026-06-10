package com.campus.modules.notification.service.impl;

import com.campus.common.context.UserContext;
import com.campus.modules.knowledge.mapper.PostMapper;
import com.campus.modules.notification.dto.NotificationResponse;
import com.campus.modules.notification.entity.Notification;
import com.campus.modules.notification.mapper.NotificationMapper;
import com.campus.modules.notification.service.NotificationService;
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
    private static final String LIKE_NOTIFICATION_TYPE = "LIKE_CREATED";

    private final NotificationMapper notificationMapper;
    private final PostMapper postMapper;

    @Override
    public void createCommentNotification(Long postId, Long actorUserId, String contentPreview) {
        createPostOwnerNotification(
                postId,
                actorUserId,
                COMMENT_NOTIFICATION_TYPE,
                buildCommentContent(contentPreview));
    }

    @Override
    public void createLikeNotification(Long postId, Long actorUserId) {
        createPostOwnerNotification(
                postId,
                actorUserId,
                LIKE_NOTIFICATION_TYPE,
                "\u6709\u4eba\u70b9\u8d5e\u4e86\u4f60\u7684\u5e16\u5b50");
    }

    @Override
    public List<NotificationResponse> listMine(Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        int safePage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = (safePage - 1) * safeSize;
        return notificationMapper.selectByReceiver(userId, offset, safeSize);
    }

    private void createPostOwnerNotification(Long postId, Long actorUserId, String type, String content) {
        Long receiverUserId = postMapper.selectPostOwnerId(postId);
        if (receiverUserId == null) {
            log.warn("skip notification because post owner not found, type={}, postId={}", type, postId);
            return;
        }
        if (receiverUserId.equals(actorUserId)) {
            log.info("skip notification for self action, type={}, postId={}, actorUserId={}", type, postId, actorUserId);
            return;
        }

        Notification notification = new Notification();
        notification.setReceiverUserId(receiverUserId);
        notification.setActorUserId(actorUserId);
        notification.setType(type);
        notification.setPostId(postId);
        notification.setContent(content);
        notificationMapper.insert(notification);

        log.info("notification created, type={}, notificationId={}, receiverUserId={}, actorUserId={}, postId={}",
                type,
                notification.getId(),
                receiverUserId,
                actorUserId,
                postId);
    }

    private String buildCommentContent(String contentPreview) {
        if (contentPreview == null || contentPreview.isBlank()) {
            return "\u6709\u4eba\u8bc4\u8bba\u4e86\u4f60\u7684\u5e16\u5b50";
        }
        return "\u6709\u4eba\u8bc4\u8bba\u4e86\u4f60\u7684\u5e16\u5b50\uff1a" + contentPreview;
    }
}
