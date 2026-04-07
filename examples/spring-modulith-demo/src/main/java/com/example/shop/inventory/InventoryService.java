package com.example.shop.inventory;

import com.example.shop.order.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 库存服务
 *
 * 通过 ApplicationModuleListener 监听其他模块的事件
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    /**
     * 初始化库存
     */
    @Transactional
    public void initializeInventory(String productName, Integer stock) {
        Inventory inventory = Inventory.builder()
                .productName(productName)
                .availableStock(stock)
                .reservedStock(0)
                .totalStock(stock)
                .build();

        inventoryRepository.save(inventory);
        log.info("初始化库存: product={}, stock={}", productName, stock);
    }

    /**
     * 扣减库存
     */
    @Transactional
    public boolean reserveStock(String productName, Integer quantity) {
        Inventory inventory = inventoryRepository.findById(productName)
                .orElseThrow(() -> new IllegalArgumentException("产品不存在: " + productName));

        if (inventory.getAvailableStock() < quantity) {
            log.warn("库存不足: product={}, requested={}, available={}",
                    productName, quantity, inventory.getAvailableStock());
            return false;
        }

        inventory.setAvailableStock(inventory.getAvailableStock() - quantity);
        inventory.setReservedStock(inventory.getReservedStock() + quantity);
        inventoryRepository.save(inventory);

        log.info("库存预留成功: product={}, quantity={}, remaining={}",
                productName, quantity, inventory.getAvailableStock());
        return true;
    }

    /**
     * 监听订单创建事件，扣减库存
     */
    @ApplicationModuleListener
    @Transactional
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("收到订单创建事件，开始扣减库存: orderId={}", event.getOrderId());

        for (Order.OrderItem item : event.getItems()) {
            boolean reserved = reserveStock(item.getProductName(), item.getQuantity());
            if (!reserved) {
                log.warn("库存扣减失败: product={}", item.getProductName());
            }
        }

        log.info("订单创建事件处理完成: orderId={}", event.getOrderId());
    }

    /**
     * 获取库存
     */
    public Inventory getInventory(String productName) {
        return inventoryRepository.findById(productName)
                .orElseThrow(() -> new IllegalArgumentException("产品不存在: " + productName));
    }

    /**
     * 获取所有库存
     */
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }
}
