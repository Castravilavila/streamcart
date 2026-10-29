package com.streamcart.order.service;

import com.streamcart.order.dto.OrderItemRequest;
import com.streamcart.order.dto.OrderItemResponse;
import com.streamcart.order.dto.OrderResponse;
import com.streamcart.order.dto.PlaceOrderRequest;
import com.streamcart.order.event.OrderPlacedEvent;
import com.streamcart.order.exception.OrderNotFoundException;
import com.streamcart.order.messaging.OrderEventProducer;
import com.streamcart.order.model.Order;
import com.streamcart.order.model.OrderItem;
import com.streamcart.order.model.OrderStatus;
import com.streamcart.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    // @Transactional wraps the whole method in a single DB transaction:
    // if anything fails after items are added, the entire save is rolled back,
    // so we never end up with a half-saved order (ACID - Atomicity).
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        Order order = Order.builder()
                .customerEmail(request.getCustomerEmail())
                .status(OrderStatus.CREATED)
                .createdAt(Instant.now())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = OrderItem.builder()
                    .productId(itemRequest.getProductId())
                    .productName(itemRequest.getProductName())
                    .quantity(itemRequest.getQuantity())
                    .price(itemRequest.getPrice())
                    .build();
            order.addItem(item);
            total = total.add(itemRequest.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        orderEventProducer.publishOrderPlaced(
                OrderPlacedEvent.builder()
                        .orderId(saved.getId())
                        .customerEmail(saved.getCustomerEmail())
                        .totalAmount(saved.getTotalAmount())
                        .createdAt(saved.getCreatedAt())
                        .build()
        );

        return toResponse(saved);
    }

    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return toResponse(order);
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .customerEmail(order.getCustomerEmail())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}
