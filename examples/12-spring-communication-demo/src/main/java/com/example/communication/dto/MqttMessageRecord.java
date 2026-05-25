package com.example.communication.dto;

import java.time.Instant;

public record MqttMessageRecord(
        String topic,
        String payload,
        String direction,
        Instant timestamp) {
}
