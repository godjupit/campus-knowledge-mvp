package com.campus.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage {
    private String eventId;
    private String eventType;
    private Long postId;
    private Long actorUserId;
    private LocalDateTime createdAt;
    private String contentPreview;
}
