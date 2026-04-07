package com.example.shop.notification;

import com.example.shop.order.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

/**
 * 通知服务
 *
 * 通过 ApplicationModuleListener 监听其他模块的事件
 */
@Service
@Slf4j
public class NotificationService {

    /**
     * 发送通知
     */
    public void sendNotification(NotificationEvent notification) {
        log.info("发送通知: type={}, recipient={}, subject={}",
                notification.getType(),
                notification.getRecipient(),
                notification.getSubject());

        // 实际实现中会调用邮件服务、短信服务等
        // 这里只是模拟

        log.info("通知发送成功: {}", notification.getContent());
    }

    /**
     * 监听订单创建事件，发送通知
     */
    @ApplicationModuleListener
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("收到订单创建事件，发送通知: orderId={}", event.getOrderId());

        NotificationEvent notification = NotificationEvent.builder()
                .recipient(event.getCustomerName() + "@example.com")
                .subject("订单确认 - " + event.getOrderNumber())
                .content(String.format(
                        "您好 %s，您的订单 %s 已创建成功！\n" +
                        "订单包含 %d 个商品。\n" +
                        "感谢您的购买！",
                        event.getCustomerName(),
                        event.getOrderNumber(),
                        event.getItems().size()))
                .type(NotificationEvent.NotificationType.EMAIL)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        sendNotification(notification);
        log.info("订单通知发送完成: orderId={}", event.getOrderId());
    }
}
