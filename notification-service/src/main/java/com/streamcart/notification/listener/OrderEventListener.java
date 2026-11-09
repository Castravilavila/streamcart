package com.streamcart.notification.listener;

import com.google.protobuf.InvalidProtocolBufferException;
import com.streamcart.notification.service.NotificationService;
import com.streamcart.proto.order.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final NotificationService notificationService;

    // groupId="notification-service" (set in application.properties) means this
    // consumer's committed offset is tracked independently - if we add more
    // instances later, Kafka splits partitions between them instead of duplicating work.
    @KafkaListener(topics = "order-placed", groupId = "notification-service")
    public void onOrderPlaced(byte[] payload) {
        OrderPlacedEvent event;
        try {
            // Same generated class the producer used - the schema in streamcart-proto is the contract.
            event = OrderPlacedEvent.parseFrom(payload);
        } catch (InvalidProtocolBufferException e) {
            // "Poison pill": a message we can never parse (e.g. a leftover JSON message from
            // before the Protobuf switch). Log and move on so the consumer doesn't get stuck
            // retrying the same offset forever. A production system would route it to a
            // dead-letter topic instead of just logging.
            log.error("Skipping malformed order-placed message ({} bytes)", payload.length, e);
            return;
        }

        try {
            notificationService.notifyOrderPlaced(event);
        } catch (Exception e) {
            log.error("Failed to process order-placed event for orderId={}", event.getOrderId(), e);
        }
    }
}
