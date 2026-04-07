package com.example.websocket.controller;

import com.example.websocket.dto.ChatMessage;
import com.example.websocket.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket 聊天控制器
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    /**
     * 发送聊天消息
     * 客户端发送到 /app/chat
     * 服务端广播到 /topic/room/{roomId}
     */
    @MessageMapping("/chat")
    public void sendMessage(@Payload ChatMessage message) {
        chatService.handleMessage(message);
    }

    /**
     * 点对点消息
     */
    @MessageMapping("/private")
    @SendToUser("/queue/messages")
    public ChatMessage sendPrivateMessage(@Payload ChatMessage message, Principal principal) {
        log.info("点对点消息: from={}, to={}", principal.getName(), message.getSender());
        return message;
    }

    /**
     * 加入房间
     */
    @MessageMapping("/room/join")
    public void joinRoom(@Payload ChatMessage message) {
        message.setType(ChatMessage.MessageType.JOIN);
        chatService.handleMessage(message);
    }

    /**
     * 离开房间
     */
    @MessageMapping("/room/leave")
    public void leaveRoom(@Payload ChatMessage message) {
        message.setType(ChatMessage.MessageType.LEAVE);
        chatService.handleMessage(message);
    }
}
