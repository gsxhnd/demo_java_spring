package com.example.business.service;

import com.example.business.dto.OrderResponse;
import com.example.business.dto.PlaceOrderRequest;
import com.example.business.entity.Order;
import com.example.business.entity.Order.OrderStatus;
import com.example.business.entity.Product;
import com.example.business.exception.InsufficientStockException;
import com.example.business.exception.ResourceNotFoundException;
import com.example.business.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final NotificationService notificationService;

    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在 - id: " + id));
        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        Product product = productService.getProductEntityForUpdate(request.getProductId());

        if (product.getStock() < request.getQuantity()) {
            throw new InsufficientStockException(
                    "库存不足 - 商品: " + product.getName() + ", 可用: " + product.getStock()
                            + ", 需要: " + request.getQuantity());
        }

        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        product.setStock(product.getStock() - request.getQuantity());
        productService.saveProductEntity(product);
        productService.evictCache(product.getId());

        Order order = Order.builder()
                .productId(product.getId())
                .quantity(request.getQuantity())
                .totalAmount(total)
                .customerEmail(request.getCustomerEmail())
                .status(OrderStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();
        Order saved = orderRepository.save(order);
        log.info("订单创建成功 - id: {}, productId: {}, total: {}",
                saved.getId(), saved.getProductId(), saved.getTotalAmount());

        CompletableFuture<String> notificationFuture =
                notificationService.sendOrderConfirmation(saved.getId(), saved.getCustomerEmail());

        OrderResponse response = OrderResponse.fromEntity(saved);
        response.setAsyncNotificationStatus("SUBMITTED");
        notificationFuture.whenComplete((result, ex) -> {
            if (ex != null) {
                log.warn("异步通知失败 - orderId: {}", saved.getId(), ex);
            } else {
                log.info("异步通知完成 - orderId: {}, result: {}", saved.getId(), result);
            }
        });
        return response;
    }
}
