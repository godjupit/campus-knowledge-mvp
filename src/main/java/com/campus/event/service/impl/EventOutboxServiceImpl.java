package com.campus.event.service.impl;

import com.campus.event.config.RabbitMqConfig;
import com.campus.event.dto.EventMessage;
import com.campus.event.entity.EventOutbox;
import com.campus.event.mapper.EventOutboxMapper;
import com.campus.event.service.EventOutboxService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventOutboxServiceImpl implements EventOutboxService {

    private static final int PUBLISH_LIMIT = 100;
    private static final int MAX_RETRY = 3;

    private final EventOutboxMapper eventOutboxMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void saveEvent(String eventType, String routingKey, EventMessage payload) {
        try {
            EventOutbox eventOutbox = new EventOutbox();
            eventOutbox.setEventType(eventType);
            eventOutbox.setRoutingKey(routingKey);
            eventOutbox.setPayload(objectMapper.writeValueAsString(payload));
            eventOutboxMapper.insert(eventOutbox);
            log.info("event saved to outbox, eventId={}, eventType={}, routingKey={}",
                    payload.getEventId(),
                    eventType,
                    routingKey);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to save event outbox", exception);
        }
    }

    @Override
    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<EventOutbox> pendingEvents = eventOutboxMapper.selectPending(PUBLISH_LIMIT, MAX_RETRY);
        if (pendingEvents.isEmpty()) {
            return;
        }

        for (EventOutbox event : pendingEvents) {
            try {
                EventMessage message = objectMapper.readValue(event.getPayload(), EventMessage.class);
                rabbitTemplate.convertAndSend(RabbitMqConfig.EVENT_EXCHANGE, event.getRoutingKey(), message);
                markSent(event.getId());
                log.info("event published, outboxId={}, eventType={}, routingKey={}, eventId={}",
                        event.getId(),
                        event.getEventType(),
                        event.getRoutingKey(),
                        message.getEventId());
            } catch (Exception exception) {
                markFailed(event.getId());
                log.warn("event publish failed, outboxId={}, eventType={}, routingKey={}",
                        event.getId(),
                        event.getEventType(),
                        event.getRoutingKey(),
                        exception);
            }
        }
    }

    @Override
    public void markSent(Long id) {
        eventOutboxMapper.markSent(id);
    }

    @Override
    public void markFailed(Long id) {
        eventOutboxMapper.markFailed(id);
    }
}
