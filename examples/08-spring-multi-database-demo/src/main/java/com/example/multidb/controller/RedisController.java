package com.example.multidb.controller;

import com.example.multidb.dto.redis.CacheUserRequest;
import com.example.multidb.dto.redis.CacheUserResponse;
import com.example.multidb.dto.redis.LockResponse;
import com.example.multidb.service.RedisCacheService;
import com.example.multidb.service.RedisLockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
@Validated
@Tag(name = "Redis", description = "Redis 缓存与分布式锁")
public class RedisController {

    private final RedisCacheService redisCacheService;
    private final RedisLockService redisLockService;

    @PostMapping("/cache")
    @Operation(summary = "写入用户缓存（带 TTL）")
    public ResponseEntity<CacheUserResponse> putCache(@Valid @RequestBody CacheUserRequest request) {
        return new ResponseEntity<>(redisCacheService.put(request), HttpStatus.CREATED);
    }

    @GetMapping("/cache/{key}")
    @Operation(summary = "读取用户缓存")
    public ResponseEntity<CacheUserResponse> getCache(
            @Parameter(description = "缓存键") @PathVariable String key) {
        return ResponseEntity.ok(redisCacheService.get(key));
    }

    @DeleteMapping("/cache/{key}")
    @Operation(summary = "删除用户缓存")
    public ResponseEntity<Void> deleteCache(
            @Parameter(description = "缓存键") @PathVariable String key) {
        redisCacheService.delete(key);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/locks/{resource}")
    @Operation(summary = "尝试获取分布式锁（SETNX + TTL）")
    public ResponseEntity<LockResponse> tryLock(
            @Parameter(description = "锁资源名") @PathVariable String resource,
            @Parameter(description = "锁过期秒数") @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "TTL 至少为 1 秒") long ttlSeconds) {
        return ResponseEntity.ok(redisLockService.tryLock(resource, ttlSeconds));
    }

    @DeleteMapping("/locks/{resource}")
    @Operation(summary = "释放分布式锁")
    public ResponseEntity<Void> releaseLock(
            @Parameter(description = "锁资源名") @PathVariable String resource) {
        redisLockService.releaseLock(resource);
        return ResponseEntity.noContent().build();
    }
}
