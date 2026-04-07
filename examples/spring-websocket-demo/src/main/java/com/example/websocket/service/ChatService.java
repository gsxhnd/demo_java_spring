package com.example.websocket.service;

import com.example.websocket.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket 聊天服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;

    // 在线用户集合
    private final Map<String, Set<String>> roomUsers = new ConcurrentHashMap<>();

    // 消息历史（简化实现，生产环境应存储到数据库）
    private final Map<String, java.util.List<ChatMessage>> messageHistory = new ConcurrentHashMap<>();

    /**
     * 处理聊天消息
     */
    public void handleMessage(ChatMessage message) {
        log.info("收到消息: room={}, sender={}, content={}",
                message.getRoomId(), message.getSender(), message.getContent());

        switch (message.getType()) {
            case CHAT -> handleChatMessage(message);
            case JOIN -> handleJoin(message);
            case LEAVE -> handleLeave(message);
            case TYPING -> handleTyping(message);
            default -> log.warn("未知消息类型: {}", message.getType());
        }
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());

        // 保存到历史记录
        messageHistory.computeIfAbsent(message.getRoomId(), k -> new java.util.ArrayList<>())
                .add(message);

        // 广播到房间
        messagingTemplate.convertAndSend(
                "/topic/room/" + message.getRoomId(),
                message
        );
    }

    /**
     * 处理用户加入
     */
    private void handleJoin(ChatMessage message) {
        // 添加用户到房间
        roomUsers.computeIfAbsent(message.getRoomId(), k -> new CopyOnWriteArraySet<>())
                .add(message.getSender());

        // 发送系统消息
        ChatMessage systemMessage = ChatMessage.builder()
                .type(ChatMessage.MessageType.SYSTEM)
                .content(message.getSender() + " 加入了房间")
                .roomId(message.getRoomId())
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/room/" + message.getRoomId(),
                systemMessage
        );

        log.info("用户 {} 加入房间 {}", message.getSender(), message.getRoomId());
    }

    /**
     * 处理用户离开
     */
    private void handleLeave(ChatMessage message) {
        // 从房间移除用户
        Set<String> users = roomUsers.get(message.getRoomId());
        if (users != null) {
            users.remove(message.getSender());
        }

        // 发送系统消息
        ChatMessage systemMessage = ChatMessage.builder()
                .type(ChatMessage.MessageType.SYSTEM)
                .content(message.getSender() + " 离开了房间")
                .roomId(message.getRoomId())
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/room/" + message.getRoomId(),
                systemMessage
        );

        log.info("用户 {} 离开房间 {}", message.getSender(), message.getRoomId());
    }

    /**
     * 处理正在输入
     */
    private void handleTyping(ChatMessage message) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + message.getRoomId() + "/typing",
                message.getSender()
        );
    }

    /**
     * 发送点对点消息
     */
    public void sendToUser(String user, ChatMessage message) {
        messagingTemplate.convertAndSendToUser(user, "/queue/messages", message);
        log.info("发送点对点消息: to={}, from={}", user, message.getSender());
    }

    /**
     * 获取房间在线用户数
     */
    public int getRoomUserCount(String roomId) {
        Set<String> users = roomUsers.get(roomId);
        return users != null ? users.size() : 0;
    }

    /**
     * 获取房间消息历史
     */
    public java.util.List<ChatMessage> getRoomHistory(String roomId) {
        return messageHistory.getOrDefault(roomId, java.util.Collections.emptyList());
    }
}
