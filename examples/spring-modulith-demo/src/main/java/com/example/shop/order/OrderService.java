package com.example.shop.order;

import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 订单服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建订单
     */
    @Transactional
    public Order createOrder(String customerName, List<Order.OrderItem> items) {
        log.info("创建订单: customer={}, items={}", customerName, items.size());

        // 计算总价
        BigDecimal totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 创建订单
        Order order = Order.builder()
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customerName(customerName)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .items(items)
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("订单创建成功: id={}, orderNumber={}", savedOrder.getId(), savedOrder.getOrderNumber());

        // 发布订单创建事件
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .customerName(savedOrder.getCustomerName())
                .items(savedOrder.getItems())
                .createdAt(savedOrder.getCreatedAt())
                .build();

        eventPublisher.publishEvent(event);
        log.info("发布订单创建事件: orderId={}", savedOrder.getId());

        return savedOrder;
    }

    /**
     * 确认订单
     */
    @Transactional
    public Order confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + orderId));

        order.setStatus(Order.OrderStatus.CONFIRMED);
        return orderRepository.save(order);
    }

    /**
     * 完成订单
     */
    @Transactional
    public Order completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + orderId));

        order.setStatus(Order.OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    /**
     * 获取订单
     */
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + orderId));
    }

    /**
     * 获取所有订单
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
