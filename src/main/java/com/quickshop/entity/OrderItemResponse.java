package com.quickshop.entity;


public record OrderItemResponse(
        Long id,
        String productId,
        Integer quantity,
        Double pricePerUnit
) {}