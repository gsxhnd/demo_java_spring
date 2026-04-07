package com.example.webflux.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 首页控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class IndexController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/info")
    public Mono<Map<String, Object>> getInfo() {
        return Mono.just(Map.of(
                "name", "Spring WebFlux Demo",
                "version", "1.0.0",
                "description", "响应式 WebFlux 演示",
                "features", new String[]{
                        "响应式 REST API",
                        "R2DBC 响应式数据库",
                        "Flux/Mono 响应式流",
                        "SSE 实时推送",
                        "WebClient 响应式 HTTP 客户端"
                }
        ));
    }

    @GetMapping("/time")
    public Mono<String> getTime() {
        return Mono.just("Current time: " + LocalDateTime.now().format(FORMATTER));
    }
}
