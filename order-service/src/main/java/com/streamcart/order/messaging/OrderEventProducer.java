package com.streamcart.order.messaging;

import com.google.protobuf.Timestamp;
import com.streamcart.order.model.Order;
import com.streamcart.proto.order.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Owns the wire format of the "order-placed" topic. The rest of order-service deals
 * only with the domain Order; the Protobuf mapping is contained here.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String TOPIC = "order-placed";

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public void publishOrderPlaced(Order order) {
        OrderPlacedEvent event = toEvent(order);

        // toByteArray() is Protobuf's binary encoding - typically several times smaller
        // than the equivalent JSON and parsed without reflection on the consumer side.
        byte[] payload = event.toByteArray();

        // Key = orderId: messages with the same key always land on the same partition,
        // which preserves ordering for all events about a given order.
        kafkaTemplate.send(TOPIC, String.valueOf(order.getId()), payload);
        log.info("Published order-placed event for orderId={} ({} bytes)", order.getId(), payload.length);
    }

    private OrderPlacedEvent toEvent(Order order) {
        return OrderPlacedEvent.newBuilder()
                .setOrderId(order.getId())
                .setCustomerEmail(order.getCustomerEmail())
                .setTotalAmount(order.getTotalAmount().toPlainString())
                .setCreatedAt(toTimestamp(order.getCreatedAt()))
                .build();
    }

    private static Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
