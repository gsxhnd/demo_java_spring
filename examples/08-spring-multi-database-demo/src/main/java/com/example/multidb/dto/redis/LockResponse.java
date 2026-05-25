package com.example.multidb.dto.redis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "分布式锁响应")
public class LockResponse {

    private String resource;
    private boolean acquired;
    private Long ttlSeconds;
}
