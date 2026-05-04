package com.example.autoconfig.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 问候服务
 *
 * 由 CustomAutoConfiguration 负责创建 Bean，不通过组件扫描
 */
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
