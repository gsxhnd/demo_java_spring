package com.example.business.repository;

import com.example.business.entity.Order;
import com.example.business.entity.Order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime before);
}
