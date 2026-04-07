package com.example.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 多级缓存服务
 *
 * 提供手动操作多级缓存的能力
 * 读取流程: L1(Caffeine) → L2(Redis) → DB
 * 写入流程: DB → L2(Redis) → L1(Caffeine)
 * 删除流程: L2(Redis) + L1(Caffeine) 都删除
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 */
@Configuration
@Slf4j
public class MultiLevelCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Cache<String, Object> caffeineCache;

    public MultiLevelCacheService(
            RedisTemplate<String, Object> redisTemplate,
            @Qualifier("userLocalCache") Cache<String, Object> caffeineCache) {
        this.redisTemplate = redisTemplate;
        this.caffeineCache = caffeineCache;
    }

    /**
     * 多级缓存读取
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @return 缓存值，如果不存在返回 null
     */
    @SuppressWarnings("unchecked")
    public <V> V get(String cacheName, String key) {
        String fullKey = buildKey(cacheName, key);

        // 1. 先查 L1 (Caffeine)
        V value = (V) caffeineCache.getIfPresent(fullKey);
        if (value != null) {
            log.debug("L1 Cache Hit: {}", fullKey);
            return value;
        }

        // 2. L1 miss，查 L2 (Redis)
        value = (V) redisTemplate.opsForValue().get(fullKey);
        if (value != null) {
            log.debug("L2 Cache Hit: {}", fullKey);
            // 回填 L1
            caffeineCache.put(fullKey, value);
            return value;
        }

        // 3. L2 也 miss，返回 null（需要从 DB 加载）
        log.debug("Cache Miss: {}", fullKey);
        return null;
    }

    /**
     * 多级缓存写入
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param value 缓存值
     * @param ttlSeconds L2 过期时间（秒）
     */
    public <V> void put(String cacheName, String key, V value, long ttlSeconds) {
        String fullKey = buildKey(cacheName, key);

        // 写入 L2 (Redis)
        redisTemplate.opsForValue().set(fullKey, value, ttlSeconds, TimeUnit.SECONDS);
        log.debug("Write L2 Cache: {}, TTL: {}s", fullKey, ttlSeconds);

        // 写入 L1 (Caffeine)
        caffeineCache.put(fullKey, value);
        log.debug("Write L1 Cache: {}", fullKey);
    }

    /**
     * 多级缓存删除
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     */
    public void evict(String cacheName, String key) {
        String fullKey = buildKey(cacheName, key);

        // 删除 L1
        caffeineCache.invalidate(fullKey);
        log.debug("Evict L1 Cache: {}", fullKey);

        // 删除 L2
        redisTemplate.delete(fullKey);
        log.debug("Evict L2 Cache: {}", fullKey);
    }

    /**
     * 清空指定缓存的所有数据
     */
    public void clear(String cacheName) {
        // 清空 L1
        caffeineCache.asMap().keySet().removeIf(key -> key.startsWith(cacheName + ":"));
        log.debug("Clear L1 Cache: {}", cacheName);

        // 清空 L2 (需要使用 pattern)
        var keys = redisTemplate.keys(cacheName + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Clear L2 Cache: {}, deleted {} keys", cacheName, keys.size());
        }
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStats getStats() {
        var stats = caffeineCache.stats();
        return new CacheStats(
                stats.hitCount(),
                stats.missCount(),
                stats.hitRate(),
                stats.evictionCount(),
                caffeineCache.estimatedSize()
        );
    }

    /**
     * 构建完整缓存键
     */
    private String buildKey(String cacheName, String key) {
        return cacheName + ":" + key;
    }

    /**
     * 缓存统计信息
     */
    public record CacheStats(
            long hitCount,
            long missCount,
            double hitRate,
            long evictionCount,
            long size
    ) {}
}
