package com.streamcart.order.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamcart.order.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String TOPIC = "order-placed";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishOrderPlaced(OrderPlacedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            // Key = orderId as string: messages with the same key always land on the
            // same Kafka partition, preserving order for that particular order's events.
            kafkaTemplate.send(TOPIC, String.valueOf(event.getOrderId()), payload);
            log.info("Published order-placed event for orderId={}", event.getOrderId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize OrderPlacedEvent for orderId={}", event.getOrderId(), e);
        }
    }
}
