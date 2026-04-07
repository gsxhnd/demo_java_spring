package com.example.autoconfig.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * 应用配置属性
 *
 * @ConfigurationProperties 绑定 application.yml 中的配置
 * - 类型安全
 * - 松散绑定（my-prop = myProp = MY_PROP）
 * - 支持 IDE 自动补全（需要 spring-boot-configuration-processor）
 */
@Data
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Greeting greeting = new Greeting();
    private final Feature feature = new Feature();
    private final Cache cache = new Cache();

    @Data
    public static class Greeting {
        /** 是否启用问候语 */
        private boolean enabled = true;

        /** 问候语消息 */
        @NotBlank(message = "问候语消息不能为空")
        private String message = "Hello";
    }

    @Data
    public static class Feature {
        /** 功能开关 */
        private boolean enabled = false;

        /** 功能限流（每秒请求数） */
        @PositiveOrZero
        private int rateLimit = 100;
    }

    @Data
    public static class Cache {
        /** 是否启用缓存 */
        private boolean enabled = true;

        /** 缓存 TTL（秒） */
        @PositiveOrZero
        private long ttl = 3600;
    }
}
