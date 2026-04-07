package com.example.redisdemo.controller;

import com.example.redisdemo.service.CacheExampleService;
import com.example.redisdemo.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
public class RedisController {

    private final RedisService redisService;
    private final CacheExampleService cacheExampleService;
    private final RedisTemplate<String, Object> redisTemplate;

    // --- String operations ---

    @PostMapping("/string")
    public Map<String, Object> setString(@RequestParam String key, @RequestParam String value) {
        redisService.set(key, value);
        return Map.of("status", "ok", "key", key);
    }

    @GetMapping("/string")
    public Map<String, Object> getString(@RequestParam String key) {
        String value = redisService.get(key);
        return Map.of("key", key, "value", value != null ? value : "null");
    }

    // --- Object operations ---

    @PostMapping("/object")
    public Map<String, Object> setObject(@RequestParam String key, @RequestBody Map<String, Object> data) {
        redisService.setObject(key, data);
        return Map.of("status", "ok", "key", key);
    }

    @GetMapping("/object")
    public Map<String, Object> getObject(@RequestParam String key) {
        Object value = redisService.getObject(key);
        return Map.of("key", key, "value", value != null ? value : "null");
    }

    // --- Simple distributed lock demo ---

    @PostMapping("/lock")
    public Map<String, Object> tryLock(@RequestParam(defaultValue = "my-lock") String lockKey,
                                       @RequestParam(defaultValue = "10") long timeoutSeconds) {
        String lockValue = "lock:" + Thread.currentThread().getId() + ":" + System.currentTimeMillis();

        // SET key value NX EX — atomic lock acquisition
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, timeoutSeconds, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(acquired)) {
            try {
                // Simulate critical section work
                Thread.sleep(100);
                return Map.of("status", "locked", "lockKey", lockKey, "lockValue", lockValue);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Map.of("status", "interrupted");
            } finally {
                // Release lock (simple version — production code should use Lua script for atomicity)
                redisTemplate.delete(lockKey);
            }
        }

        return Map.of("status", "failed", "message", "Could not acquire lock");
    }

    // --- Cache demo endpoints (delegates to CacheExampleService) ---

    @GetMapping("/cache/{id}")
    public Map<String, Object> getCachedUser(@PathVariable String id) {
        return cacheExampleService.getUserById(id);
    }

    @PutMapping("/cache/{id}")
    public Map<String, Object> updateCachedUser(@PathVariable String id, @RequestParam String name) {
        return cacheExampleService.updateUser(id, name);
    }

    @DeleteMapping("/cache/{id}")
    public Map<String, Object> evictCachedUser(@PathVariable String id) {
        cacheExampleService.deleteUser(id);
        return Map.of("status", "evicted", "id", id);
    }
}
