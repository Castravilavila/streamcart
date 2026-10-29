package com.streamcart.notification.service;

import com.streamcart.notification.dto.OrderNotification;
import com.streamcart.notification.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyOrderPlaced(OrderPlacedEvent event) {
        // Real SMTP credentials aren't configured for this learning project, so we
        // log what would be sent instead of calling a real JavaMailSender.
        log.info("Simulated email to {}: your order #{} for {} has been placed.",
                event.getCustomerEmail(), event.getOrderId(), event.getTotalAmount());

        OrderNotification notification = OrderNotification.builder()
                .orderId(event.getOrderId())
                .customerEmail(event.getCustomerEmail())
                .totalAmount(event.getTotalAmount())
                .message("Your order #" + event.getOrderId() + " has been placed successfully.")
                .build();

        // Pushes to every client currently subscribed to /topic/notifications in real time.
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
}
