package com.example.async.listener;

import com.example.async.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 订单事件监听器
 */
@Component
@Slf4j
public class OrderListener {

    /**
     * 同步处理订单创建事件 - 库存扣减
     */
    @EventListener
    public void handleOrderCreatedForInventory(OrderCreatedEvent event) {
        log.info("[OrderListener-Inventory] 处理订单创建: orderId={}, 扣减库存",
                event.getOrderId());
        // 同步执行库存扣减
    }

    /**
     * 异步处理订单创建事件 - 发送通知
     */
    @Async("asyncExecutor")
    @EventListener
    public void handleOrderCreatedForNotification(OrderCreatedEvent event) {
        log.info("[OrderListener-Notification] 处理订单创建: orderId={}, 发送通知",
                event.getOrderId());

        try {
            // 模拟发送通知
            Thread.sleep(500);
            log.info("[OrderListener-Notification] 通知发送完成: orderId={}", event.getOrderId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 异步处理订单创建事件 - 更新统计
     */
    @org.springframework.scheduling.annotation.Async("ioExecutor")
    @EventListener
    public void handleOrderCreatedForStatistics(OrderCreatedEvent event) {
        log.info("[OrderListener-Statistics] 处理订单创建: orderId={}, 更新统计",
                event.getOrderId());

        try {
            // 模拟统计更新
            Thread.sleep(300);
            log.info("[OrderListener-Statistics] 统计更新完成: orderId={}", event.getOrderId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
