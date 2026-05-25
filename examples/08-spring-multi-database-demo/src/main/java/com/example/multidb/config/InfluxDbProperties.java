package com.example.multidb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.influx")
public class InfluxDbProperties {

    private String url;
    private String token;
    private String org;
    private String bucket;
}
