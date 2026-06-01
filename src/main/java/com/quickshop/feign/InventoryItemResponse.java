package com.quickshop.feign;

public record InventoryItemResponse(
     Long id,
     String productId,
     Integer availableQty,
     Integer reservedQty
) {}