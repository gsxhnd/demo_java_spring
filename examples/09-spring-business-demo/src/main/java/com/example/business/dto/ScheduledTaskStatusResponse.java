package com.example.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledTaskStatusResponse {

    private LocalDateTime lastRunAt;
    private int lastCancelledCount;
    private String cronExpression;
}
