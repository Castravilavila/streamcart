package com.streamcart.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * The payload pushed to WebSocket subscribers on /topic/notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderNotification {

    private Long orderId;
    private String customerEmail;
    private BigDecimal totalAmount;
    private String message;
}
