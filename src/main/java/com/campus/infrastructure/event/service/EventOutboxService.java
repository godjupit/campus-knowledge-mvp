package com.campus.infrastructure.event.service;

import com.campus.infrastructure.event.dto.EventMessage;

public interface EventOutboxService {

    void saveEvent(String eventType, String routingKey, EventMessage payload);

    void publishPendingEvents();

    void markSent(Long id);

    void markFailed(Long id);
}
