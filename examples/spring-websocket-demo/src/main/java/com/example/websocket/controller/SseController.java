package com.example.websocket.controller;

import com.example.websocket.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * SSE 控制器
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class SseController {

    private final SseService sseService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * 建立 SSE 连接
     */
    @GetMapping(value = "/api/sse/connect/{clientId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@PathVariable String clientId) {
        log.info("SSE 连接请求: clientId={}", clientId);
        return sseService.createEmitter(clientId);
    }

    /**
     * 发送消息到指定客户端
     */
    @PostMapping("/api/sse/send/{clientId}")
    public Map<String, Object> sendToClient(
            @PathVariable String clientId,
            @RequestParam String eventName,
            @RequestParam String data) {

        sseService.sendToClient(clientId, eventName, data);

        return Map.of(
                "success", true,
                "clientId", clientId,
                "eventName", eventName
        );
    }

    /**
     * 广播消息
     */
    @PostMapping("/api/sse/broadcast")
    public Map<String, Object> broadcast(
            @RequestParam String eventName,
            @RequestParam String data) {

        sseService.broadcast(eventName, data);

        return Map.of(
                "success", true,
                "eventName", eventName,
                "connections", sseService.getConnectionCount()
        );
    }

    /**
     * 获取连接统计
     */
    @GetMapping("/api/sse/stats")
    public Map<String, Object> getStats() {
        return Map.of(
                "connections", sseService.getConnectionCount(),
                "timestamp", LocalDateTime.now().format(FORMATTER)
        );
    }

    /**
     * 发送实时时间流
     */
    @GetMapping(value = "/api/sse/time-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter timeStream() {
        SseEmitter emitter = new SseEmitter();

        Thread.ofVirtual().start(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    String time = LocalDateTime.now().format(FORMATTER);
                    emitter.send(SseEmitter.event()
                            .name("time")
                            .data("{\"time\":\"" + time + "\"}"));
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                log.debug("SSE 时间流结束");
            }
        });

        emitter.onCompletion(() -> log.debug("SSE 时间流完成"));
        emitter.onTimeout(emitter::complete);

        return emitter;
    }
}
