package com.quickshop.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quickshop.entity.CreateOrderRequest;
import com.quickshop.entity.Order;
import com.quickshop.entity.OrderCreatedEvent;
import com.quickshop.entity.OrderItem;
import com.quickshop.entity.OrderItemResponse;
import com.quickshop.entity.OrderResponse;
import com.quickshop.entity.OrderStatus;
import com.quickshop.repository.OrderRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

 private final OrderRepository orderRepository;
 private final KafkaTemplate<String, String> kafkaTemplate;
 private final OrderEventSerializer eventSerializer;

 // Simple thread pool – demo for ExecutorService usage
 private final ExecutorService executorService = Executors.newFixedThreadPool(4);

 private static final String ORDER_CREATED_TOPIC = "order-queue";

 @Transactional
 public OrderResponse createOrder(CreateOrderRequest request)  throws  Exception {
     Instant now = Instant.now();

     Order order = Order.builder()
             .externalOrderId(request.externalOrderId())
             .status(OrderStatus.CREATED)
             .createdAt(now)
             .updatedAt(now)
             .build();

     request.items().forEach(itemReq -> {
         OrderItem item = OrderItem.builder()
                 .productId(itemReq.productId())
                 .quantity(itemReq.quantity())
                 .pricePerUnit(100.0) // TODO: fetch price from product service
                 .build();
         order.addItem(item);
     });

     Order saved = orderRepository.save(order);

     CompletableFuture<Void> asyncSideEffect = CompletableFuture.runAsync(() -> {

         try {
             Thread.sleep(50);
         } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
         }
     }, executorService);

     // Build event
     OrderCreatedEvent event = new OrderCreatedEvent(
             UUID.randomUUID().toString(),
             saved.getId().toString(),
             saved.getExternalOrderId(),
             saved.getCreatedAt(),
             saved.getItems().stream()
                     .map(i -> new OrderCreatedEvent.OrderCreatedItem(i.getProductId(), i.getQuantity()))
                     .collect(toList())
     );

     String payload = eventSerializer.toJson(event);

     kafkaTemplate.send(ORDER_CREATED_TOPIC, saved.getId().toString(), payload).get();

     // optionally wait for asyncSideEffect if needed
     asyncSideEffect.whenComplete((v, ex) -> {
         log.info("EVENT SENT");
         log.debug("Order Payload={}", payload);
     });

     System.out.println("EVENT SENT");
     System.out.println(payload);
     return toResponse(saved);
 }

 @Transactional(readOnly = true)
 public OrderResponse getOrder(Long id) {
     Order order = orderRepository.findById(id)
             .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));
     return toResponse(order);
 }

 @Transactional(readOnly = true)
 public Page<OrderResponse> listOrders(OrderStatus status, Instant from, Instant to, Pageable pageable) {
     return orderRepository.findByStatusAndCreatedAtBetween(status, from, to, pageable)
             .map(this::toResponse);
 }

 private OrderResponse toResponse(Order order) {
     List<OrderItemResponse> items = order.getItems().stream()
             .map(i -> new OrderItemResponse(
                     i.getId(),
                     i.getProductId(),
                     i.getQuantity(),
                     i.getPricePerUnit()
             ))
             .collect(toList());

     return new OrderResponse(
             order.getId(),
             order.getExternalOrderId(),
             order.getStatus().name(),
             order.getCreatedAt(),
             order.getUpdatedAt(),
             items
     );
 }
}