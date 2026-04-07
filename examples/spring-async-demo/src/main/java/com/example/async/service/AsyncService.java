package com.example.async.service;

import com.example.async.event.EmailSendEvent;
import com.example.async.event.OrderCreatedEvent;
import com.example.async.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 异步服务
 * 演示 @Async、CompletableFuture 和事件驱动
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncService {

    private final ApplicationEventPublisher eventPublisher;
    private final Random random = new Random();

    // ========== @Async 示例 ==========

    /**
     * 异步发送邮件（void 返回值）
     */
    @Async("asyncExecutor")
    public void sendEmailAsync(String to, String subject, String content) {
        log.info("[Async] 开始发送邮件: to={}, subject={}", to, subject);
        try {
            TimeUnit.MILLISECONDS.sleep(1000);  // 模拟耗时操作
            log.info("[Async] 邮件发送完成: to={}", to);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Async] 邮件发送失败: to={}", to, e);
        }
    }

    /**
     * 异步发送邮件（Future 返回值）
     */
    @Async("asyncExecutor")
    public CompletableFuture<Boolean> sendEmailWithFuture(String to, String subject) {
        log.info("[Async-Future] 开始发送邮件: to={}", to);
        try {
            TimeUnit.MILLISECONDS.sleep(500);
            log.info("[Async-Future] 邮件发送成功: to={}", to);
            return CompletableFuture.completedFuture(true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * 使用指定执行器
     */
    @Async("ioExecutor")
    public CompletableFuture<String> processFile(String fileName) {
        log.info("[Async-IO] 开始处理文件: {}", fileName);
        try {
            TimeUnit.MILLISECONDS.sleep(2000);
            log.info("[Async-IO] 文件处理完成: {}", fileName);
            return CompletableFuture.completedFuture("processed:" + fileName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture(null);
        }
    }

    // ========== CompletableFuture 示例 ==========

    /**
     * 异步获取用户信息
     */
    public CompletableFuture<String> getUserAsync(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("[CompletableFuture] 获取用户信息: userId={}", userId);
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "User-" + userId;
        });
    }

    /**
     * 异步获取订单信息
     */
    public CompletableFuture<String> getOrderAsync(Long orderId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("[CompletableFuture] 获取订单信息: orderId={}", orderId);
            try {
                TimeUnit.MILLISECONDS.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Order-" + orderId;
        });
    }

    /**
     * 并行执行多个异步任务，然后组合结果
     */
    public CompletableFuture<String> getUserOrderSummary(Long userId, Long orderId) {
        CompletableFuture<String> userFuture = getUserAsync(userId);
        CompletableFuture<String> orderFuture = getOrderAsync(orderId);

        // 等待两个都完成
        return userFuture.thenCombine(orderFuture, (user, order) -> {
            log.info("[CompletableFuture] 组合结果: user={}, order={}", user, order);
            return user + " -> " + order;
        });
    }

    /**
     * 并行执行多个任务，等待所有完成
     */
    public CompletableFuture<Void> processMultipleTasks() {
        CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
            log.info("[CompletableFuture] Task 1 开始");
            sleep(300);
            return "Task1-Done";
        });

        CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> {
            log.info("[CompletableFuture] Task 2 开始");
            sleep(500);
            return "Task2-Done";
        });

        CompletableFuture<String> task3 = CompletableFuture.supplyAsync(() -> {
            log.info("[CompletableFuture] Task 3 开始");
            sleep(200);
            return "Task3-Done";
        });

        // 等待所有任务完成
        return CompletableFuture.allOf(task1, task2, task3)
                .thenRun(() -> log.info("[CompletableFuture] 所有任务完成"));
    }

    // ========== 事件驱动示例 ==========

    /**
     * 发布订单创建事件
     */
    public void createOrder(Long userId, Double amount) {
        Long orderId = random.nextLong(10000);

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderId)
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .userId(String.valueOf(userId))
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();

        log.info("[Event] 发布订单创建事件: orderId={}", orderId);
        eventPublisher.publishEvent(event);
    }

    /**
     * 发布用户注册事件
     */
    public void registerUser(String username, String email) {
        Long userId = random.nextLong(10000);

        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(userId)
                .username(username)
                .email(email)
                .registeredAt(LocalDateTime.now())
                .build();

        log.info("[Event] 发布用户注册事件: userId={}", userId);
        eventPublisher.publishEvent(event);
    }

    /**
     * 发布邮件发送事件
     */
    public void sendEmail(String to, String subject, String content, EmailSendEvent.EmailType type) {
        EmailSendEvent event = EmailSendEvent.builder()
                .to(to)
                .subject(subject)
                .content(content)
                .type(type)
                .build();

        log.info("[Event] 发布邮件发送事件: to={}", to);
        eventPublisher.publishEvent(event);
    }

    // ========== 工具方法 ==========

    private void sleep(int millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
