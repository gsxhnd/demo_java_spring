package com.example.communication.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.mqtt")
public class MqttProperties {

    private boolean enabled = true;
    private String brokerUrl = "tcp://localhost:1883";
    private String clientId = "spring-communication-demo";
    private String defaultTopicPrefix = "demo/communication";
    private List<String> subscribeTopics = List.of(
            "demo/communication/sensor/#",
            "demo/communication/command/#");
}
