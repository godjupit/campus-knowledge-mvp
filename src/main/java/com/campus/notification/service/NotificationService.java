package com.campus.notification.service;

import com.campus.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    void createCommentNotification(Long postId, Long actorUserId, String contentPreview);

    List<NotificationResponse> listMine(Integer page, Integer size);
}
