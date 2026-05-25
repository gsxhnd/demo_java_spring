package com.example.communication.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ModbusWriteRequest {

    @Min(0)
    @Max(65535)
    private int value;
}
