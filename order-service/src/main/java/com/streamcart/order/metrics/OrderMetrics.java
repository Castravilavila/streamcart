package com.streamcart.order.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Business metrics for orders. Infrastructure metrics (HTTP, JVM, Kafka) come for free
 * from Spring Boot Actuator; these answer questions about the business itself.
 *
 * Micrometer naming uses dots ("orders.placed"); the Prometheus registry converts them
 * to underscores and appends a unit/type suffix, so these show up in Prometheus as:
 *   orders_placed_total          (Counter)
 *   orders_amount_sum / _count / _max   (DistributionSummary)
 *
 * Meters are created once here rather than looked up on every call - registering a meter
 * is a map lookup with locking, and this keeps the service code free of metric plumbing.
 */
@Component
public class OrderMetrics {

    private final Counter ordersPlaced;
    private final DistributionSummary orderAmount;

    public OrderMetrics(MeterRegistry registry) {
        this.ordersPlaced = Counter.builder("orders.placed")
                .description("Total number of orders successfully placed")
                .register(registry);

        this.orderAmount = DistributionSummary.builder("orders.amount")
                .description("Monetary value of placed orders")
                .baseUnit("currency")
                .register(registry);
    }

    public void recordOrderPlaced(BigDecimal totalAmount) {
        ordersPlaced.increment();
        orderAmount.record(totalAmount.doubleValue());
    }
}
