package com.example.communication.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MqttPublishRequest {

    @NotBlank
    private String deviceId;

    private String topic;

    @NotBlank
    private String payload;

    @Min(0)
    @Max(2)
    private int qos = 1;
}
