package com.streamcart.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamcart.notification.event.OrderPlacedEvent;
import com.streamcart.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    // groupId="notification-service" (set in application.properties) means this
    // consumer's committed offset is tracked independently - if we add more
    // instances later, Kafka splits partitions between them instead of duplicating work.
    @KafkaListener(topics = "order-placed", groupId = "notification-service")
    public void onOrderPlaced(String payload) {
        try {
            OrderPlacedEvent event = objectMapper.readValue(payload, OrderPlacedEvent.class);
            notificationService.notifyOrderPlaced(event);
        } catch (Exception e) {
            log.error("Failed to process order-placed message: {}", payload, e);
        }
    }
}
