package com.example.multidb.dto.mongo;

import com.example.multidb.entity.mongo.UserLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户行为日志响应")
public class UserLogResponse {

    private String id;
    private Long userId;
    private String action;
    private Map<String, Object> details;
    private Instant createdAt;

    public static UserLogResponse fromEntity(UserLog log) {
        return UserLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .action(log.getAction())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
