package com.example.communication.config;

import com.intelligt.modbus.jlibmodbus.data.DataHolder;
import com.intelligt.modbus.jlibmodbus.data.ModbusHoldingRegisters;
import com.intelligt.modbus.jlibmodbus.slave.ModbusSlave;
import com.intelligt.modbus.jlibmodbus.slave.ModbusSlaveFactory;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ModbusSimulatorConfig {

    private final ModbusProperties properties;
    private ModbusSlave slave;

    @Bean
    public ModbusSlave modbusTcpSlave() throws Exception {
        TcpParameters tcpParameters = new TcpParameters();
        tcpParameters.setHost(InetAddress.getByName(properties.getHost()));
        tcpParameters.setPort(properties.getPort());
        tcpParameters.setKeepAlive(true);

        slave = ModbusSlaveFactory.createModbusSlaveTCP(tcpParameters);
        DataHolder dataHolder = new DataHolder();
        ModbusHoldingRegisters holding = new ModbusHoldingRegisters(properties.getRegisterCount());
        holding.set(0, 2500);
        holding.set(1, 65);
        holding.set(2, 0);
        holding.set(3, 2200);
        dataHolder.setHoldingRegisters(holding);
        slave.setDataHolder(dataHolder);
        slave.setServerAddress(properties.getSlaveId());
        slave.listen();
        log.info("Modbus TCP slave listening on {}:{}", properties.getHost(), properties.getPort());
        return slave;
    }

    @PreDestroy
    public void shutdown() {
        if (slave != null) {
            try {
                slave.setDataHolder(new DataHolder());
                slave.shutdown();
            } catch (Exception ex) {
                log.warn("Modbus slave shutdown: {}", ex.getMessage());
            }
        }
    }
}
