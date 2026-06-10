package com.campus.modules.notification.service;

import com.campus.modules.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    void createCommentNotification(Long postId, Long actorUserId, String contentPreview);

    void createLikeNotification(Long postId, Long actorUserId);

    List<NotificationResponse> listMine(Integer page, Integer size);
}
