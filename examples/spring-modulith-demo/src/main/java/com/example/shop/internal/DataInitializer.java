package com.example.shop.internal;

import com.example.shop.inventory.Inventory;
import com.example.shop.inventory.InventoryRepository;
import com.example.shop.inventory.InventoryService;
import com.example.shop.notification.NotificationService;
import com.example.shop.order.Order;
import com.example.shop.order.Order.OrderItem;
import com.example.shop.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

/**
 * 初始化配置
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final InventoryService inventoryService;
    private final OrderService orderService;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // 初始化库存
            initializeInventory();

            // 创建示例订单
            createSampleOrders();

            log.info("模块化单体数据初始化完成");
        };
    }

    private void initializeInventory() {
        inventoryService.initializeInventory("MacBook Pro", 50);
        inventoryService.initializeInventory("iPhone 15", 200);
        inventoryService.initializeInventory("AirPods Pro", 500);
        inventoryService.initializeInventory("iPad Air", 150);
        inventoryService.initializeInventory("Apple Watch", 80);
        log.info("库存初始化完成: {} 个产品", 5);
    }

    private void createSampleOrders() {
        // 订单 1
        Order order1 = Order.builder()
                .orderNumber("ORD-SAMPLE01")
                .customerName("张三")
                .status(Order.OrderStatus.PENDING)
                .totalAmount(new BigDecimal("25997.00"))
                .items(List.of(
                        OrderItem.builder().productName("MacBook Pro").quantity(1).price(new BigDecimal("14999.00")).build(),
                        OrderItem.builder().productName("AirPods Pro").quantity(1).price(new BigDecimal("1899.00")).build(),
                        OrderItem.builder().productName("Apple Watch").quantity(1).price(new BigDecimal("2999.00")).build()
                ))
                .build();
        orderService.createOrder("张三", order1.getItems());

        // 订单 2
        Order order2 = Order.builder()
                .orderNumber("ORD-SAMPLE02")
                .customerName("李四")
                .status(Order.OrderStatus.PENDING)
                .totalAmount(new BigDecimal("8999.00"))
                .items(List.of(
                        OrderItem.builder().productName("iPhone 15").quantity(1).price(new BigDecimal("8999.00")).build()
                ))
                .build();
        orderService.createOrder("李四", order2.getItems());

        log.info("示例订单创建完成: {} 个订单", 2);
    }
}
