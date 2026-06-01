package com.quickshop.feign;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inventory-service",url = "${spring.inventory.url}")
public interface InventoryClient {


    @GetMapping("/api/inventory/{productId}")
    InventoryItemResponse getInventory(
            @PathVariable String productId);

}
