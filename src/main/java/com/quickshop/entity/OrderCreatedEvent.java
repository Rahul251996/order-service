package com.quickshop.entity;

import java.time.Instant;
import java.util.List;

// Immutable event (record)
public record OrderCreatedEvent(
        String eventId,
        String orderId,
        String externalOrderId,
        Instant createdAt,
        List<OrderCreatedItem> items
) {
    public record OrderCreatedItem(
            String productId,
            Integer quantity
    ) {}
}