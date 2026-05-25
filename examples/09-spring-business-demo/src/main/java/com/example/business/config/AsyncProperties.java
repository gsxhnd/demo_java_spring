package com.example.business.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "app.async")
public class AsyncProperties {

    @Min(1)
    private int corePoolSize = 2;

    @Min(1)
    private int maxPoolSize = 5;

    @Min(0)
    private int queueCapacity = 50;

    private String threadNamePrefix = "biz-async-";
}
