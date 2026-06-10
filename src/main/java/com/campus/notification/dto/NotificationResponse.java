package com.campus.notification.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private Long actorUserId;
    private String actorUsername;
    private String type;
    private Long postId;
    private String content;
    private Integer readFlag;
    private LocalDateTime createdAt;
}
