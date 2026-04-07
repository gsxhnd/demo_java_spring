package com.example.security.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * 公开接口示例
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    /**
     * 公开接口 - 任何人都可以访问
     */
    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello, this is a public endpoint!");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 公开接口 - 获取公开信息
     */
    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of(
                "app", "Spring Security Demo",
                "version", "1.0.0",
                "description", "Spring Security + JWT Authentication Demo"
        );
    }
}
