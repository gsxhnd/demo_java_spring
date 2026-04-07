package com.example.shop.internal;

import com.example.shop.inventory.Inventory;
import com.example.shop.inventory.InventoryService;
import com.example.shop.order.Order;
import com.example.shop.order.Order.OrderItem;
import com.example.shop.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API 控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final OrderService orderService;
    private final InventoryService inventoryService;

    // ========== 订单接口 ==========

    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody Map<String, Object> request) {
        String customerName = (String) request.get("customerName");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsMap = (List<Map<String, Object>>) request.get("items");

        List<OrderItem> items = itemsMap.stream()
                .map(m -> OrderItem.builder()
                        .productName((String) m.get("productName"))
                        .quantity((Integer) m.get("quantity"))
                        .price(new BigDecimal(m.get("price").toString()))
                        .build())
                .toList();

        Order order = orderService.createOrder(customerName, items);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @PostMapping("/orders/{id}/confirm")
    public ResponseEntity<Order> confirmOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.confirmOrder(id));
    }

    @PostMapping("/orders/{id}/complete")
    public ResponseEntity<Order> completeOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.completeOrder(id));
    }

    // ========== 库存接口 ==========

    @GetMapping("/inventory")
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/inventory/{productName}")
    public ResponseEntity<Inventory> getInventory(@PathVariable String productName) {
        return ResponseEntity.ok(inventoryService.getInventory(productName));
    }

    // ========== 模块信息 ==========

    @GetMapping("/modules")
    public ResponseEntity<Map<String, Object>> getModuleInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "Spring Modulith Demo");
        info.put("modules", List.of(
                Map.of("name", "order", "description", "订单管理模块"),
                Map.of("name", "inventory", "description", "库存管理模块"),
                Map.of("name", "notification", "description", "通知模块"),
                Map.of("name", "internal", "description", "内部初始化模块")
        ));
        info.put("features", List.of(
                "模块边界清晰",
                "事件驱动通信",
                "事务一致性"
        ));
        return ResponseEntity.ok(info);
    }
}
