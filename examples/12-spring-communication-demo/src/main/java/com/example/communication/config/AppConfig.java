package com.example.communication.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({MqttProperties.class, ModbusProperties.class})
public class AppConfig {
}
