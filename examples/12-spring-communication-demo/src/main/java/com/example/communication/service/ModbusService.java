package com.example.communication.service;

import com.example.communication.config.ModbusProperties;
import com.example.communication.dto.ModbusRegisterResponse;
import com.example.communication.dto.ModbusStatusResponse;
import com.example.communication.dto.ModbusWriteRequest;
import com.intelligt.modbus.jlibmodbus.Modbus;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusNumberException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusProtocolException;
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModbusService {

    private final ModbusProperties properties;
    private ModbusMaster master;

    public ModbusStatusResponse status() {
        boolean connected = master != null && master.isConnected();
        return ModbusStatusResponse.builder()
                .host(properties.getHost())
                .port(properties.getPort())
                .slaveId(properties.getSlaveId())
                .connected(connected)
                .simulatorMode(true)
                .build();
    }

    public ModbusRegisterResponse readHoldingRegisters() {
        ensureConnected();
        try {
            int[] values = master.readHoldingRegisters(
                    properties.getSlaveId(),
                    properties.getHoldingRegisterOffset(),
                    properties.getRegisterCount());
            List<ModbusRegisterResponse.RegisterValue> registers = new ArrayList<>();
            for (int i = 0; i < values.length; i++) {
                int address = 40001 + properties.getHoldingRegisterOffset() + i;
                registers.add(new ModbusRegisterResponse.RegisterValue(
                        address,
                        properties.getHoldingRegisterOffset() + i,
                        values[i],
                        describeRegister(i, values[i])));
            }
            return ModbusRegisterResponse.builder()
                    .slaveId(properties.getSlaveId())
                    .registers(registers)
                    .build();
        } catch (ModbusProtocolException | ModbusIOException | ModbusNumberException ex) {
            disconnectQuietly();
            throw new IllegalStateException("Modbus read failed: " + ex.getMessage(), ex);
        }
    }

    public void writeHoldingRegister(int offset, ModbusWriteRequest request) {
        ensureConnected();
        try {
            master.writeSingleRegister(properties.getSlaveId(), offset, request.getValue());
        } catch (ModbusProtocolException | ModbusIOException | ModbusNumberException ex) {
            disconnectQuietly();
            throw new IllegalStateException("Modbus write failed: " + ex.getMessage(), ex);
        }
    }

    private void ensureConnected() {
        try {
            if (master == null) {
                Modbus.setLogLevel(Modbus.LogLevel.LEVEL_RELEASE);
                TcpParameters tcpParameters = new TcpParameters();
                tcpParameters.setHost(InetAddress.getByName(properties.getHost()));
                tcpParameters.setPort(properties.getPort());
                tcpParameters.setKeepAlive(true);
                master = ModbusMasterFactory.createModbusMasterTCP(tcpParameters);
                master.setResponseTimeout(3000);
            }
            if (!master.isConnected()) {
                master.connect();
            }
        } catch (ModbusIOException | UnknownHostException ex) {
            throw new IllegalStateException(
                    "Cannot connect Modbus master to " + properties.getHost() + ":" + properties.getPort(), ex);
        }
    }

    private void disconnectQuietly() {
        if (master != null && master.isConnected()) {
            try {
                master.disconnect();
            } catch (ModbusIOException ex) {
                log.warn("Modbus disconnect: {}", ex.getMessage());
            }
        }
    }

    @PreDestroy
    public void destroy() {
        disconnectQuietly();
    }

    private String describeRegister(int index, int value) {
        return switch (index) {
            case 0 -> "temperature (x0.1 °C)";
            case 1 -> "humidity (%)";
            case 2 -> "pump status (0=off, 1=on)";
            case 3 -> "voltage (x0.01 V)";
            default -> "raw value";
        };
    }
}
