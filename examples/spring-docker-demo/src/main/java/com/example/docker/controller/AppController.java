package com.example.docker.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 应用信息控制器
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class AppController {

    @Value("${spring.application.name:spring-docker-demo}")
    private String appName;

    @Value("${server.port:8080}")
    private int serverPort;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取应用信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getAppInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", appName);
        info.put("version", "1.0.0");
        info.put("description", "Spring Boot Docker Deployment Demo");
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("serverPort", serverPort);
        info.put("timestamp", LocalDateTime.now().format(FORMATTER));

        return ResponseEntity.ok(info);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("app", appName);
        health.put("timestamp", LocalDateTime.now().format(FORMATTER));

        return ResponseEntity.ok(health);
    }

    /**
     * 获取系统信息
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> system = new HashMap<>();
        system.put("osName", System.getProperty("os.name"));
        system.put("osVersion", System.getProperty("os.version"));
        system.put("osArch", System.getProperty("os.arch"));
        system.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        system.put("maxMemory", Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");
        system.put("totalMemory", Runtime.getRuntime().totalMemory() / (1024 * 1024) + " MB");
        system.put("freeMemory", Runtime.getRuntime().freeMemory() / (1024 * 1024) + " MB");
        system.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime() / 1000 + " seconds");

        return ResponseEntity.ok(system);
    }

    /**
     * Docker 环境信息
     */
    @GetMapping("/docker")
    public ResponseEntity<Map<String, Object>> getDockerInfo() {
        Map<String, Object> docker = new HashMap<>();

        // 检测是否在 Docker 容器中
        boolean inDocker = checkInDocker();
        docker.put("inDocker", inDocker);
        docker.put("hostname", getHostname());

        // 从环境变量获取容器信息
        docker.put("containerId", System.getenv("HOSTNAME"));
        docker.put("podName", System.getenv("POD_NAME"));

        Map<String, Object> response = new HashMap<>();
        response.put("docker", docker);
        response.put("timestamp", LocalDateTime.now().format(FORMATTER));

        return ResponseEntity.ok(response);
    }

    /**
     * 欢迎页面
     */
    @GetMapping("/welcome")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> welcome = new HashMap<>();
        welcome.put("message", "Welcome to Spring Boot Docker Demo!");
        welcome.put("app", appName);
        welcome.put("features", new String[]{
                "Multi-stage Docker build",
                "JVM container optimization",
                "Health check configuration",
                "Graceful shutdown"
        });
        welcome.put("links", Map.of(
                "/api/info", "应用信息",
                "/api/health", "健康检查",
                "/api/system", "系统信息",
                "/api/docker", "Docker 信息",
                "/actuator/health", "Actuator 健康检查"
        ));

        return ResponseEntity.ok(welcome);
    }

    /**
     * 检查是否运行在 Docker 容器中
     */
    private boolean checkInDocker() {
        // 检查 cgroup 文件
        try {
            String cgroup = "/proc/1/cgroup";
            if (new java.io.File(cgroup).exists()) {
                String content = java.nio.file.Files.readString(java.nio.file.Paths.get(cgroup));
                return content.contains("docker") || content.contains("containerd");
            }
        } catch (Exception e) {
            log.debug("检查 Docker 环境失败", e);
        }

        // 检查 .dockerenv 文件
        return new java.io.File("/.dockerenv").exists();
    }

    /**
     * 获取主机名
     */
    private String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
