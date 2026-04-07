package com.example.redisdemo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;

// Demonstrates Spring Cache annotations backed by Redis
@Slf4j
@Service
public class CacheExampleService {

    // Result is cached; subsequent calls with the same id skip this method
    @Cacheable(value = "users", key = "#id")
    public Map<String, Object> getUserById(String id) {
        log.info("Cache miss - fetching user: {}", id);
        // Simulate a slow data source lookup
        return Map.of("id", id, "name", "user-" + id, "source", "database");
    }

    // Always executes and updates the cache with the return value
    @CachePut(value = "users", key = "#id")
    public Map<String, Object> updateUser(String id, String name) {
        log.info("Updating user: {} -> {}", id, name);
        return Map.of("id", id, "name", name, "source", "updated");
    }

    // Removes the entry from the cache
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(String id) {
        log.info("Evicting user from cache: {}", id);
    }
}
