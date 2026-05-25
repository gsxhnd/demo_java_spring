package com.example.multidb.service;

import com.example.multidb.dto.redis.CacheUserRequest;
import com.example.multidb.dto.redis.CacheUserResponse;
import com.example.multidb.entity.redis.CachedUser;
import com.example.multidb.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private static final String KEY_PREFIX = "cache:user:";

    private final RedisTemplate<String, CachedUser> cachedUserRedisTemplate;

    public CacheUserResponse put(CacheUserRequest request) {
        String redisKey = normalizeKey(request.getKey());
        CachedUser user = CachedUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .age(request.getAge())
                .build();
        Duration ttl = Duration.ofSeconds(request.getTtlSeconds());
        cachedUserRedisTemplate.opsForValue().set(redisKey, user, ttl);
        log.info("Redis 缓存写入 - key: {}, ttl: {}s", redisKey, request.getTtlSeconds());
        return CacheUserResponse.of(request.getKey(), user, request.getTtlSeconds());
    }

    public CacheUserResponse get(String key) {
        String redisKey = normalizeKey(key);
        CachedUser user = cachedUserRedisTemplate.opsForValue().get(redisKey);
        if (user == null) {
            throw new ResourceNotFoundException("缓存不存在或已过期 - key: " + key);
        }
        Long ttl = cachedUserRedisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        return CacheUserResponse.of(key, user, ttl != null && ttl >= 0 ? ttl : null);
    }

    public void delete(String key) {
        String redisKey = normalizeKey(key);
        Boolean deleted = cachedUserRedisTemplate.delete(redisKey);
        if (!Boolean.TRUE.equals(deleted)) {
            throw new ResourceNotFoundException("缓存不存在 - key: " + key);
        }
        log.info("Redis 缓存删除 - key: {}", redisKey);
    }

    private String normalizeKey(String key) {
        if (key.startsWith(KEY_PREFIX)) {
            return key;
        }
        return KEY_PREFIX + key;
    }
}
