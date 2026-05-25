package com.example.communication.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProtocolRecommendRequest {

    @NotNull
    private Scenario scenario;

    public enum Scenario {
        WEB_API,
        MICROSERVICE_INTERNAL,
        BROWSER_REALTIME,
        IOT_SENSORS,
        INDUSTRIAL_PLC,
        LARGE_FILE_TRANSFER
    }
}
