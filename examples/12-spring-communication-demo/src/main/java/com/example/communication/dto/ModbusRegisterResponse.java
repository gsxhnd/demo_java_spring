package com.example.communication.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ModbusRegisterResponse {

    private int slaveId;
    private List<RegisterValue> registers;

    public record RegisterValue(int modbusAddress, int offset, int value, String description) {
    }
}
