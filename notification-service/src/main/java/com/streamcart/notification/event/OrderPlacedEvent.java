package com.streamcart.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Mirrors order-service's event.OrderPlacedEvent. Kept as a separate class
 * here (rather than a shared library) because each service should be able
 * to evolve its own view of the event independently - this is a normal
 * trade-off in event-driven microservices.
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
