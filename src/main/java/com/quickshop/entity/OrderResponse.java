package com.quickshop.entity;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String externalOrderId,
        String status,
        Instant createdAt,
        Instant updatedAt,
        List<OrderItemResponse> items
) {}