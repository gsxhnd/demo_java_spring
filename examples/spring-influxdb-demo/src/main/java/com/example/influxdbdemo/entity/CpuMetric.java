package com.example.influxdbdemo.entity;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;

import java.time.Instant;

// POJO mapped to InfluxDB "cpu" measurement
@Data
@Measurement(name = "cpu")
public class CpuMetric {

    @Column(tag = true)
    private String host;

    @Column(tag = true)
    private String region;

    @Column
    private Double usageUser;

    @Column
    private Double usageSystem;

    @Column
    private Double usageIdle;

    @Column(timestamp = true)
    private Instant time;
}
