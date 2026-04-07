package com.example.autoconfig.service;

import com.example.autoconfig.properties.AppProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * 特性服务
 *
 * 演示根据 Profile 和配置动态启用功能
 */
@Service
public class FeatureService {

    private final AppProperties.Feature feature;

    public FeatureService(AppProperties properties) {
        this.feature = properties.getFeature();
        System.out.println("[FeatureService] 功能配置: enabled=" + feature.isEnabled()
                + ", rateLimit=" + feature.getRateLimit());
    }

    public boolean isFeatureEnabled() {
        return feature.isEnabled();
    }

    public int getRateLimit() {
        return feature.getRateLimit();
    }

    /**
     * 应用启动完成后执行
     *
     * 替代 @PostConstruct 的另一种方式，
     * 确保所有 Bean 初始化完成后执行
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("[FeatureService] 应用已就绪，功能状态: "
                + (feature.isEnabled() ? "启用" : "禁用"));
    }
}
