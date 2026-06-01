package com.quickshop.controller;

import com.quickshop.common.entity.AuthenticatedUser;
import com.quickshop.common.entity.UserPrincipal;
import com.quickshop.feign.InventoryClient;
import com.quickshop.feign.InventoryItemResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.quickshop.entity.CreateOrderRequest;
import com.quickshop.entity.OrderResponse;
import com.quickshop.entity.OrderStatus;
import com.quickshop.service.OrderService;

import java.time.Instant;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

 private final OrderService orderService;

 @Autowired
 private InventoryClient inventoryClient;

 @PostMapping(name = "/createOrder")
 public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) throws Exception {
     return orderService.createOrder(request);
 }

 @GetMapping("/{id}")
 public OrderResponse getOrder(@PathVariable Long id) {
     return orderService.getOrder(id);
 }

 @GetMapping("getAllOrders")
 public Page<OrderResponse> listOrders(
         @RequestParam OrderStatus status,
         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
         Pageable pageable
 ) {
     return orderService.listOrders(status, from, to, pageable);
 }
    @GetMapping("/me")
    public Object me(Authentication authentication) {

        return authentication;
    }

    @GetMapping("/testFeign/{productId}")
    InventoryItemResponse getInventory(@PathVariable String productId) {
      return inventoryClient.getInventory(productId);
    }
    @GetMapping("/current-user")
    public UserPrincipal currentUser(
            Authentication authentication) {

        Jwt jwt =
                (Jwt) authentication.getPrincipal();

        return UserPrincipal.builder()
                .userId(jwt.getSubject())
                .username(
                        jwt.getClaimAsString(
                                "preferred_username"))
                .email(
                        jwt.getClaimAsString(
                                "email"))
                .build();
    }


}