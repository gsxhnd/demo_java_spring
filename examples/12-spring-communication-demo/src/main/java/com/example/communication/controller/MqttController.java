package com.example.communication.controller;

import com.example.communication.dto.MqttMessageRecord;
import com.example.communication.dto.MqttPublishRequest;
import com.example.communication.dto.MqttStatusResponse;
import com.example.communication.service.MqttService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mqtt")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.mqtt", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MqttController {

    private final MqttService mqttService;

    @GetMapping("/status")
    public ResponseEntity<MqttStatusResponse> status() {
        return ResponseEntity.ok(mqttService.status());
    }

    @PostMapping("/publish")
    public ResponseEntity<Map<String, String>> publish(@Valid @RequestBody MqttPublishRequest request) {
        mqttService.publish(request);
        return ResponseEntity.ok(Map.of(
                "status", "published",
                "deviceId", request.getDeviceId()));
    }

    @GetMapping("/messages")
    public ResponseEntity<List<MqttMessageRecord>> recent(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(mqttService.recentMessages(limit));
    }
}
