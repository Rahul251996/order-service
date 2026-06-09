package com.quickshop.service;

import com.quickshop.entity.*;
import com.quickshop.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private OrderEventSerializer eventSerializer;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrder_success() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(
                "EXT-123",
                List.of(new OrderItemRequest("P1001", 2))
        );

        Order savedOrder = Order.builder()
                .id(1L)
                .externalOrderId("EXT-123")
                .status(OrderStatus.CREATED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .items(List.of(OrderItem.builder()
                        .id(10L)
                        .productId("P1001")
                        .quantity(2)
                        .pricePerUnit(100.0)
                        .build()))
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(eventSerializer.toJson(any(OrderCreatedEvent.class))).thenReturn("{\"event\":\"orderCreated\"}");

        // Act
        OrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals("EXT-123", response.externalOrderId());
        assertEquals(1, response.items().size());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testGetOrder_found() {
        Order order = Order.builder()
                .id(1L)
                .externalOrderId("EXT-123")
                .status(OrderStatus.CREATED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrder(1L);

        assertEquals("EXT-123", response.externalOrderId());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testGetOrder_notFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> orderService.getOrder(99L));
    }
}
