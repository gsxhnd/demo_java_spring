package com.example.websocket.controller;

import com.example.websocket.dto.ChatMessage;
import com.example.websocket.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API 控制器（用于获取历史消息等）
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatApiController {

    private final ChatService chatService;

    /**
     * 获取房间消息历史
     */
    @GetMapping("/rooms/{roomId}/history")
    public ResponseEntity<List<ChatMessage>> getRoomHistory(@PathVariable String roomId) {
        return ResponseEntity.ok(chatService.getRoomHistory(roomId));
    }

    /**
     * 获取房间在线人数
     */
    @GetMapping("/rooms/{roomId}/users/count")
    public ResponseEntity<Map<String, Object>> getRoomUserCount(@PathVariable String roomId) {
        return ResponseEntity.ok(Map.of(
                "roomId", roomId,
                "onlineUsers", chatService.getRoomUserCount(roomId)
        ));
    }

    /**
     * 发送系统消息（通过 REST）
     */
    @PostMapping("/rooms/{roomId}/broadcast")
    public ResponseEntity<Map<String, Object>> broadcastMessage(
            @PathVariable String roomId,
            @RequestParam String content) {

        ChatMessage message = ChatMessage.builder()
                .type(ChatMessage.MessageType.SYSTEM)
                .content(content)
                .roomId(roomId)
                .build();

        chatService.handleMessage(message);

        return ResponseEntity.ok(Map.of("success", true));
    }
}
