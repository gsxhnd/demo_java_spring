package com.example.websocket.service;

import com.example.websocket.dto.SseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SSE 服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {

    // 保存所有活跃的 SseEmitter
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * 创建 SSE 连接
     */
    public SseEmitter createEmitter(String clientId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitters.computeIfAbsent(clientId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // 回调处理
        emitter.onCompletion(() -> {
            log.info("SSE 连接完成: clientId={}", clientId);
            removeEmitter(clientId, emitter);
        });

        emitter.onTimeout(() -> {
            log.info("SSE 连接超时: clientId={}", clientId);
            removeEmitter(clientId, emitter);
        });

        emitter.onError(e -> {
            log.error("SSE 连接错误: clientId={}", clientId, e);
            removeEmitter(clientId, emitter);
        });

        // 发送初始连接成功消息
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"status\":\"connected\",\"clientId\":\"" + clientId + "\"}"));
        } catch (IOException e) {
            log.error("发送初始消息失败", e);
        }

        log.info("创建 SSE 连接: clientId={}", clientId);
        return emitter;
    }

    /**
     * 发送消息到指定客户端
     */
    public void sendToClient(String clientId, String eventName, Object data) {
        CopyOnWriteArrayList<SseEmitter> clientEmitters = emitters.get(clientId);
        if (clientEmitters == null || clientEmitters.isEmpty()) {
            log.warn("客户端未连接: {}", clientId);
            return;
        }

        for (SseEmitter emitter : clientEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
                log.debug("SSE 消息发送成功: clientId={}, event={}", clientId, eventName);
            } catch (IOException e) {
                log.error("SSE 消息发送失败: clientId={}", clientId, e);
                removeEmitter(clientId, emitter);
            }
        }
    }

    /**
     * 广播消息到所有客户端
     */
    public void broadcast(String eventName, Object data) {
        log.debug("广播 SSE 消息: event={}, clients={}", eventName, emitters.size());

        for (Map.Entry<String, CopyOnWriteArrayList<SseEmitter>> entry : emitters.entrySet()) {
            String clientId = entry.getKey();
            for (SseEmitter emitter : entry.getValue()) {
                try {
                    emitter.send(SseEmitter.event()
                            .name(eventName)
                            .data(data));
                } catch (IOException e) {
                    log.error("广播消息失败: clientId={}", clientId, e);
                    removeEmitter(clientId, emitter);
                }
            }
        }
    }

    /**
     * 移除 emitter
     */
    private void removeEmitter(String clientId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> clientEmitters = emitters.get(clientId);
        if (clientEmitters != null) {
            clientEmitters.remove(emitter);
            if (clientEmitters.isEmpty()) {
                emitters.remove(clientId);
            }
        }
    }

    /**
     * 模拟发送实时通知（定时任务）
     */
    @Scheduled(fixedRate = 10000)
    public void sendPeriodicNotification() {
        String notification = String.format(
                "{\"type\":\"notification\",\"message\":\"定时通知\",\"time\":\"%s\"}",
                LocalDateTime.now().format(FORMATTER)
        );
        broadcast("notification", notification);
    }

    /**
     * 获取连接数
     */
    public int getConnectionCount() {
        return emitters.values().stream()
                .mapToInt(CopyOnWriteArrayList::size)
                .sum();
    }
}
