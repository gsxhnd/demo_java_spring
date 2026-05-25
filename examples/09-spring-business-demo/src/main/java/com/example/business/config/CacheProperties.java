package com.example.business.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

    private boolean enabled = true;

    @Min(1)
    private int ttlSeconds = 300;

    private String redisKeyPrefix = "biz:cache:";
}
