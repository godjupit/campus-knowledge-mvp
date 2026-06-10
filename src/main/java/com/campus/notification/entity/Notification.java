package com.campus.notification.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Notification {
    private Long id;
    private Long receiverUserId;
    private Long actorUserId;
    private String type;
    private Long postId;
    private String content;
    private Integer readFlag;
    private LocalDateTime createdAt;
}
