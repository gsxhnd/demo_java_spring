package com.example.async.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 模拟业务服务
 */
@Service
@Slf4j
public class BusinessService {

    /**
     * 模拟获取用户数据
     */
    public String getUserData(Long userId) {
        log.info("[Business] 获取用户数据: userId={}", userId);
        simulateDelay(500);
        return String.format("UserData(%d)", userId);
    }

    /**
     * 模拟获取产品数据
     */
    public String getProductData(Long productId) {
        log.info("[Business] 获取产品数据: productId={}", productId);
        simulateDelay(800);
        return String.format("ProductData(%d)", productId);
    }

    /**
     * 模拟发送通知
     */
    public void sendNotification(String userId, String message) {
        log.info("[Business] 发送通知: userId={}, message={}", userId, message);
        simulateDelay(300);
    }

    /**
     * 模拟发送邮件
     */
    public boolean sendEmail(String to, String subject) {
        log.info("[Business] 发送邮件: to={}, subject={}", to, subject);
        simulateDelay(1000);
        return true;
    }

    /**
     * 演示 MDC 上下文传递
     */
    public CompletableFuture<String> processWithTraceId(Long id) {
        String traceId = MDC.get("traceId");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString().substring(0, 8);
        }

        return CompletableFuture.supplyAsync(() -> {
            // 在异步线程中设置 MDC
            MDC.put("traceId", traceId);
            try {
                log.info("[Process] 开始处理: id={}, traceId={}", id, traceId);
                simulateDelay(500);
                log.info("[Process] 处理完成: id={}, traceId={}", id, traceId);
                return "Processed-" + id;
            } finally {
                MDC.remove("traceId");
            }
        });
    }

    private void simulateDelay(int millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
