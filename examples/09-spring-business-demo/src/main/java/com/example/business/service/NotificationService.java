package com.example.business.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class NotificationService {

    @Async("taskExecutor")
    public CompletableFuture<String> sendOrderConfirmation(Long orderId, String email) {
        String taskId = UUID.randomUUID().toString();
        log.info("[Async] 发送订单确认邮件 - taskId: {}, orderId: {}, email: {}", taskId, orderId, email);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture("INTERRUPTED:" + taskId);
        }
        log.info("[Async] 邮件发送完成 - taskId: {}, orderId: {}", taskId, orderId);
        return CompletableFuture.completedFuture("SENT:" + taskId);
    }
}
