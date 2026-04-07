package com.example.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private MessageType type;
    private String sender;
    private String content;
    private String roomId;
    private LocalDateTime timestamp;

    public enum MessageType {
        CHAT,           // 普通聊天消息
        JOIN,           // 加入房间
        LEAVE,          // 离开房间
        TYPING,         // 正在输入
        SYSTEM          // 系统消息
    }

    public static ChatMessage of(String sender, String content) {
        return ChatMessage.builder()
                .type(MessageType.CHAT)
                .sender(sender)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
