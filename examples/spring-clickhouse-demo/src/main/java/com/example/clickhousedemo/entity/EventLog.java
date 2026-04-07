package com.example.clickhousedemo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventLog {
    private Long id;
    private String eventType;
    private Long userId;
    private String eventData;
    private LocalDateTime eventTime;
}
