package com.example.communication.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MqttStatusResponse {

    private boolean enabled;
    private String brokerUrl;
    private String clientId;
    private List<String> subscribeTopics;
    private boolean connected;
}
