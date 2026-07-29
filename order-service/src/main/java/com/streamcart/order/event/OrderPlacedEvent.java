package com.streamcart.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * The payload published to the Kafka "order-placed" topic.
 * Kept intentionally small/flat - consumers (e.g. notification-service)
 * only need enough info to notify the customer, not the full order graph.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {

    private Long orderId;
    private String customerEmail;
    private BigDecimal totalAmount;
    private Instant createdAt;
}
