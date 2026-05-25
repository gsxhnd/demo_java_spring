package com.example.communication.dto;

public record ProtocolMatrixRow(
        String protocol,
        String communicationMode,
        String messageDirection,
        String transport,
        String messageFormat,
        String browserSupport,
        String typicalScenario) {
}
