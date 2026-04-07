package com.example.security.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 受保护的普通用户接口示例
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    /**
     * 用户首页 - 需要 USER 或 ADMIN 角色
     */
    @GetMapping("/home")
    public Map<String, Object> home(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to user home page!");
        response.put("username", principal.getName());
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Map<String, Object> profile(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return response;
    }

    /**
     * 用户设置 - 需要 USER 角色
     */
    @GetMapping("/settings")
    @PreAuthorize("hasRole('USER')")
    public Map<String, String> settings() {
        return Map.of(
                "theme", "light",
                "language", "zh-CN",
                "notifications", "enabled"
        );
    }

    /**
     * 消息列表
     */
    @GetMapping("/messages")
    public Map<String, Object> messages() {
        Map<String, Object> response = new HashMap<>();
        response.put("messages", List.of(
                Map.of("id", 1, "content", "Welcome to Spring Security Demo!"),
                Map.of("id", 2, "content", "This is a protected user endpoint."),
                Map.of("id", 3, "content", "You need to be authenticated to access this.")
        ));
        return response;
    }
}
