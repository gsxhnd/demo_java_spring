package com.example.communication.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/websocket")
public class WebSocketInfoController {

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        return ResponseEntity.ok(Map.of(
                "stompEndpoint", "/ws",
                "sockJsEnabled", true,
                "applicationPrefix", "/app",
                "broadcastTopic", "/topic/public",
                "userQueue", "/user/queue/notifications",
                "sendPublic", "/app/chat.send",
                "sendPrivateNotify", "/app/chat.notify",
                "demoPage", "/chat.html"));
    }
}
