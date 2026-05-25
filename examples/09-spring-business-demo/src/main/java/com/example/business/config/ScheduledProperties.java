package com.example.business.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.scheduled")
public class ScheduledProperties {

    private String cancelPendingOrdersCron = "0 */1 * * * *";
}
