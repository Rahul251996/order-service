// com/example/order/api/dto/OrderItemRequest.java
package com.quickshop.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record OrderItemRequest(
        @NotBlank String productId,
        @Min(1) Integer quantity
) {}