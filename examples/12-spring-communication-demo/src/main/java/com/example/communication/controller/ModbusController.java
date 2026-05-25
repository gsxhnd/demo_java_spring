package com.example.communication.controller;

import com.example.communication.dto.ModbusRegisterResponse;
import com.example.communication.dto.ModbusStatusResponse;
import com.example.communication.dto.ModbusWriteRequest;
import com.example.communication.service.ModbusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/modbus")
@RequiredArgsConstructor
public class ModbusController {

    private final ModbusService modbusService;

    @GetMapping("/status")
    public ResponseEntity<ModbusStatusResponse> status() {
        return ResponseEntity.ok(modbusService.status());
    }

    @GetMapping("/registers")
    public ResponseEntity<ModbusRegisterResponse> registers() {
        return ResponseEntity.ok(modbusService.readHoldingRegisters());
    }

    @PostMapping("/registers/{offset}")
    public ResponseEntity<Map<String, Object>> write(
            @PathVariable int offset,
            @Valid @RequestBody ModbusWriteRequest request) {
        modbusService.writeHoldingRegister(offset, request);
        return ResponseEntity.ok(Map.of(
                "offset", offset,
                "value", request.getValue()));
    }
}
