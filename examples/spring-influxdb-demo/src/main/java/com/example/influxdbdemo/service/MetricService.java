package com.example.influxdbdemo.service;

import com.example.influxdbdemo.entity.CpuMetric;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetricService {

    private final InfluxDBClient influxDBClient;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String org;

    public MetricService(InfluxDBClient influxDBClient) {
        this.influxDBClient = influxDBClient;
    }

    // Write a single metric using blocking API
    public void writeMetric(CpuMetric metric) {
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        writeApi.writeMeasurement(WritePrecision.NS, metric);
    }

    // Batch write metrics using async WriteApi
    public void batchWriteMetrics(List<CpuMetric> metrics) {
        try (WriteApi writeApi = influxDBClient.makeWriteApi()) {
            for (CpuMetric metric : metrics) {
                writeApi.writeMeasurement(WritePrecision.NS, metric);
            }
        }
    }

    // Query CPU metrics from the last hour for a given host
    public List<CpuMetric> queryLastHour(String host) {
        String flux = String.format(
                "from(bucket: \"%s\")" +
                " |> range(start: -1h)" +
                " |> filter(fn: (r) => r._measurement == \"cpu\")" +
                " |> filter(fn: (r) => r.host == \"%s\")" +
                " |> pivot(rowKey: [\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")",
                bucket, host);

        QueryApi queryApi = influxDBClient.getQueryApi();
        return queryApi.query(flux, org, CpuMetric.class);
    }

    // Query aggregated metrics with a configurable window
    public List<CpuMetric> queryAggregated(String host, int windowMinutes) {
        String flux = String.format(
                "from(bucket: \"%s\")" +
                " |> range(start: -1h)" +
                " |> filter(fn: (r) => r._measurement == \"cpu\")" +
                " |> filter(fn: (r) => r.host == \"%s\")" +
                " |> aggregateWindow(every: %dm, fn: mean, createEmpty: false)" +
                " |> pivot(rowKey: [\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")",
                bucket, host, windowMinutes);

        QueryApi queryApi = influxDBClient.getQueryApi();
        return queryApi.query(flux, org, CpuMetric.class);
    }
}
