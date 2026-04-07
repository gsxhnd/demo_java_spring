package com.example.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSE 事件数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SseEvent {

    private String eventType;
    private String data;
    private Long id;
    private Integer retry;

    public static SseEvent of(String eventType, String data) {
        return SseEvent.builder()
                .eventType(eventType)
                .data(data)
                .build();
    }
}
