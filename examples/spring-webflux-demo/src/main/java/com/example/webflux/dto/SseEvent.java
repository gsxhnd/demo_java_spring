package com.example.webflux.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SSE 事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SseEvent {

    private String event;
    private String data;
    private LocalDateTime timestamp;

    public static SseEvent of(String event, String data) {
        return SseEvent.builder()
                .event(event)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
