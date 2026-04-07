package com.example.autoconfig.service;

import com.example.autoconfig.properties.AppProperties;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 问候服务
 *
 * 演示通过 @ConfigurationProperties 注入配置
 */
@Service
public class GreetingService {

    private final String message;
    private final Map<String, Long> cache = new ConcurrentHashMap<>();

    public GreetingService(String message) {
        this.message = message;
    }

    public String greet(String name) {
        String key = "greet:" + name;
        Long cached = cache.get(key);
        if (cached != null) {
            return message + ", " + name + "! (cached)";
        }
        cache.put(key, System.currentTimeMillis());
        return message + ", " + name + "!";
    }

    public String getMessage() {
        return message;
    }

    public void clearCache() {
        cache.clear();
    }
}
