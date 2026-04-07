package com.example.security.controller;

import com.example.security.entity.User;
import com.example.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员接口示例
 * 只有 ADMIN 角色的用户才能访问
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    /**
     * 管理后台首页
     */
    @GetMapping("/home")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> adminHome() {
        return Map.of(
                "message", "Welcome to Admin Dashboard!",
                "version", "1.0.0"
        );
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        List<User> users = userRepository.findAll();

        List<Map<String, Object>> userList = users.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("email", user.getEmail());
                    userMap.put("roles", user.getRoles());
                    userMap.put("enabled", user.isEnabled());
                    userMap.put("createdAt", user.getCreatedAt());
                    return userMap;
                })
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("users", userList);
        response.put("total", userList.size());

        return ResponseEntity.ok(response);
    }

    /**
     * 获取系统统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(User::isEnabled)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", totalUsers - activeUsers);
        stats.put("timestamp", System.currentTimeMillis());

        return stats;
    }

    /**
     * 启用/禁用用户
     */
    @PatchMapping("/users/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setEnabled(!user.isEnabled());
                    userRepository.save(user);

                    Map<String, Object> response = new HashMap<>();
                    response.put("id", user.getId());
                    response.put("username", user.getUsername());
                    response.put("enabled", user.isEnabled());
                    response.put("message", user.isEnabled() ? "用户已启用" : "用户已禁用");

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
