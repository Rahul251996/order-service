// com/example/order/repository/OrderRepository.java
package com.quickshop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.quickshop.entity.Order;
import com.quickshop.entity.OrderStatus;

import java.time.Instant;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByStatusAndCreatedAtBetween(
            OrderStatus status,
            Instant from,
            Instant to,
            Pageable pageable
    );
}