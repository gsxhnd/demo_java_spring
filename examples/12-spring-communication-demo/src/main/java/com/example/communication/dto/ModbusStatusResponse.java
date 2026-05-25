package com.example.communication.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModbusStatusResponse {

    private String host;
    private int port;
    private int slaveId;
    private boolean connected;
    private boolean simulatorMode;
}
