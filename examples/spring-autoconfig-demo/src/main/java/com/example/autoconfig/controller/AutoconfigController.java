package com.example.autoconfig.controller;

import com.example.autoconfig.dto.ApiResponse;
import com.example.autoconfig.properties.AppProperties;
import com.example.autoconfig.service.FeatureService;
import com.example.autoconfig.service.GreetingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 自动配置演示控制器
 */
@RestController
@RequestMapping("/api/autoconfig")
public class AutoconfigController {

    private final GreetingService greetingService;
    private final FeatureService featureService;
    private final AppProperties appProperties;
    private final Environment environment;

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    @Value("${server.port:8080}")
    private int serverPort;

    public AutoconfigController(GreetingService greetingService,
                                FeatureService featureService,
                                AppProperties appProperties,
                                Environment environment) {
        this.greetingService = greetingService;
        this.featureService = featureService;
        this.appProperties = appProperties;
        this.environment = environment;
    }

    /**
     * 获取问候语
     */
    @GetMapping("/greet")
    public ApiResponse<Map<String, String>> greet(
            @RequestParam(required = false, defaultValue = "World") String name) {
        String greeting = greetingService.greet(name);

        Map<String, String> result = new HashMap<>();
        result.put("greeting", greeting);
        result.put("message", appProperties.getGreeting().getMessage());

        return ApiResponse.success(result);
    }

    /**
     * 获取应用信息
     */
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getAppInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", applicationName);
        info.put("port", serverPort);
        info.put("profiles", String.join(", ", environment.getActiveProfiles()));
        info.put("featureEnabled", featureService.isFeatureEnabled());
        info.put("cacheEnabled", appProperties.getCache().isEnabled());
        info.put("cacheTtl", appProperties.getCache().getTtl());

        return ApiResponse.success(info);
    }

    /**
     * 获取配置属性（演示 @ConfigurationProperties 绑定）
     */
    @GetMapping("/properties")
    public ApiResponse<AppProperties> getProperties() {
        return ApiResponse.success(appProperties);
    }

    /**
     * 动态获取配置（演示 Environment）
     */
    @GetMapping("/env/{key}")
    public ApiResponse<String> getEnv(@PathVariable String key) {
        String value = environment.getProperty(key, "未找到");
        return ApiResponse.success(value);
    }

    /**
     * 清除缓存
     */
    @PostMapping("/cache/clear")
    public ApiResponse<Void> clearCache() {
        greetingService.clearCache();
        return ApiResponse.success("缓存已清除", null);
    }
}
