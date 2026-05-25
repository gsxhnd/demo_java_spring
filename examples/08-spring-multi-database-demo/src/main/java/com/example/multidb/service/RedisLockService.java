package com.example.multidb.service;

import com.example.multidb.dto.redis.LockResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockService {

    private static final String LOCK_PREFIX = "lock:";

    private final StringRedisTemplate stringRedisTemplate;

    public LockResponse tryLock(String resource, long ttlSeconds) {
        String lockKey = LOCK_PREFIX + resource;
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "locked", Duration.ofSeconds(ttlSeconds));
        boolean success = Boolean.TRUE.equals(acquired);
        log.info("Redis 分布式锁 - resource: {}, acquired: {}", resource, success);
        return LockResponse.builder()
                .resource(resource)
                .acquired(success)
                .ttlSeconds(ttlSeconds)
                .build();
    }

    public void releaseLock(String resource) {
        String lockKey = LOCK_PREFIX + resource;
        stringRedisTemplate.delete(lockKey);
        log.info("Redis 分布式锁释放 - resource: {}", resource);
    }
}
