package com.example.async.controller;

import com.example.async.event.EmailSendEvent;
import com.example.async.service.AsyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 异步处理控制器
 */
@RestController
@RequestMapping("/api/async")
@RequiredArgsConstructor
public class AsyncController {

    private final AsyncService asyncService;

    // ========== @Async 示例 ==========

    /**
     * 异步发送邮件（void 返回）
     */
    @PostMapping("/email/async")
    public ResponseEntity<Map<String, Object>> sendEmailAsync(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String content) {

        asyncService.sendEmailAsync(to, subject, content);

        return ResponseEntity.ok(Map.of(
                "message", "邮件发送请求已提交",
                "to", to,
                "status", "PROCESSING"
        ));
    }

    /**
     * 异步发送邮件（Future 返回）
     */
    @PostMapping("/email/future")
    public ResponseEntity<Map<String, Object>> sendEmailWithFuture(@RequestParam String to,
                                                                      @RequestParam String subject) {
        CompletableFuture<Boolean> future = asyncService.sendEmailWithFuture(to, subject);

        return ResponseEntity.ok(Map.of(
                "message", "邮件发送任务已提交",
                "to", to,
                "status", "PROCESSING"
        ));
    }

    /**
     * 异步处理文件
     */
    @PostMapping("/file/process")
    public ResponseEntity<Map<String, Object>> processFile(@RequestParam String fileName) {
        asyncService.processFile(fileName);

        return ResponseEntity.ok(Map.of(
                "message", "文件处理任务已提交",
                "fileName", fileName
        ));
    }

    // ========== CompletableFuture 示例 ==========

    /**
     * 获取用户订单汇总（thenCombine 示例）
     */
    @GetMapping("/user-order/{userId}/{orderId}")
    public ResponseEntity<CompletableFuture<String>> getUserOrderSummary(
            @PathVariable Long userId,
            @PathVariable Long orderId) {

        CompletableFuture<String> result = asyncService.getUserOrderSummary(userId, orderId);
        return ResponseEntity.ok(result);
    }

    /**
     * 并行处理多个任务
     */
    @PostMapping("/batch/process")
    public ResponseEntity<Map<String, Object>> processBatch() {
        long startTime = System.currentTimeMillis();

        asyncService.processMultipleTasks().join();

        long duration = System.currentTimeMillis() - startTime;

        return ResponseEntity.ok(Map.of(
                "message", "批量处理完成",
                "duration", duration + "ms"
        ));
    }

    /**
     * 并行获取多个资源
     */
    @GetMapping("/parallel-fetch")
    public ResponseEntity<Map<String, Object>> parallelFetch() {
        long startTime = System.currentTimeMillis();

        String user = asyncService.getUserAsync(1L).join();
        String order = asyncService.getOrderAsync(100L).join();

        long duration = System.currentTimeMillis() - startTime;

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("order", order);
        result.put("duration", duration + "ms");
        result.put("status", "COMPLETED");

        return ResponseEntity.ok(result);
    }

    // ========== 事件驱动示例 ==========

    /**
     * 创建订单（发布事件）
     */
    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestParam Long userId,
            @RequestParam Double amount) {

        asyncService.createOrder(userId, amount);

        return ResponseEntity.ok(Map.of(
                "message", "订单创建成功，异步处理已触发",
                "userId", userId,
                "amount", amount
        ));
    }

    /**
     * 用户注册（发布事件）
     */
    @PostMapping("/users/register")
    public ResponseEntity<Map<String, Object>> registerUser(
            @RequestParam String username,
            @RequestParam String email) {

        asyncService.registerUser(username, email);

        return ResponseEntity.ok(Map.of(
                "message", "用户注册成功，后续处理异步进行",
                "username", username,
                "email", email
        ));
    }

    /**
     * 发送邮件（发布事件）
     */
    @PostMapping("/email/event")
    public ResponseEntity<Map<String, Object>> sendEmailEvent(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String content,
            @RequestParam(defaultValue = "NOTIFICATION") EmailSendEvent.EmailType type) {

        asyncService.sendEmail(to, subject, content, type);

        return ResponseEntity.ok(Map.of(
                "message", "邮件发送事件已发布",
                "to", to,
                "type", type.name()
        ));
    }

    // ========== 健康检查 ==========

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "spring-async-demo"
        ));
    }
}
