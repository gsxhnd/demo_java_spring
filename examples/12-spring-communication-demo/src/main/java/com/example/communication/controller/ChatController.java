package com.example.communication.controller;

import com.example.communication.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    @SendTo("/topic/public")
    public ChatMessage sendPublic(@Payload ChatMessage message) {
        return enrich(message);
    }

    @MessageMapping("/chat.notify")
    public void notifyUser(@Payload ChatMessage message) {
        ChatMessage enriched = enrich(message);
        if (enriched.getSender() != null && !enriched.getSender().isBlank()) {
            messagingTemplate.convertAndSendToUser(
                    enriched.getSender(),
                    "/queue/notifications",
                    enriched);
        }
    }

    private ChatMessage enrich(ChatMessage message) {
        if (message.getTimestamp() == null) {
            message.setTimestamp(Instant.now());
        }
        return message;
    }
}
