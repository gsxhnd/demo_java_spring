package com.example.communication.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.modbus")
public class ModbusProperties {

    private String host = "127.0.0.1";
    private int port = 1502;
    private int slaveId = 1;
    private int holdingRegisterOffset = 0;
    private int registerCount = 4;
}
