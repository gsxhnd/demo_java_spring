package com.example.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog {
    private Long id;
    private String operation;
    private String entityType;
    private Long entityId;
    private String details;
    private LocalDateTime createTime;
}
