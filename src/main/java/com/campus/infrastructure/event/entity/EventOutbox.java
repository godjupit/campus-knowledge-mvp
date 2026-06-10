package com.campus.infrastructure.event.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventOutbox {
    private Long id;
    private String eventType;
    private String routingKey;
    private String payload;
    private String status;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
