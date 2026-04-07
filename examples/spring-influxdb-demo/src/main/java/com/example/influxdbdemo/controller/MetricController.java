package com.example.influxdbdemo.controller;

import com.example.influxdbdemo.entity.CpuMetric;
import com.example.influxdbdemo.service.MetricService;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.HealthCheck;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
public class MetricController {

    private final MetricService metricService;
    private final InfluxDBClient influxDBClient;

    public MetricController(MetricService metricService, InfluxDBClient influxDBClient) {
        this.metricService = metricService;
        this.influxDBClient = influxDBClient;
    }

    // Write a single CPU metric
    @PostMapping
    public ResponseEntity<String> writeMetric(@RequestBody CpuMetric metric) {
        if (metric.getTime() == null) {
            metric.setTime(Instant.now());
        }
        metricService.writeMetric(metric);
        return ResponseEntity.ok("Metric written");
    }

    // Batch write multiple CPU metrics
    @PostMapping("/batch")
    public ResponseEntity<String> batchWrite(@RequestBody List<CpuMetric> metrics) {
        metrics.forEach(m -> {
            if (m.getTime() == null) {
                m.setTime(Instant.now());
            }
        });
        metricService.batchWriteMetrics(metrics);
        return ResponseEntity.ok("Batch written: " + metrics.size() + " metrics");
    }

    // Query metrics from the last hour
    @GetMapping
    public ResponseEntity<List<CpuMetric>> queryLastHour(@RequestParam String host) {
        List<CpuMetric> results = metricService.queryLastHour(host);
        return ResponseEntity.ok(results);
    }

    // Query aggregated metrics with a configurable window
    @GetMapping("/aggregated")
    public ResponseEntity<List<CpuMetric>> queryAggregated(
            @RequestParam String host,
            @RequestParam(defaultValue = "5") int window) {
        List<CpuMetric> results = metricService.queryAggregated(host, window);
        return ResponseEntity.ok(results);
    }

    // InfluxDB health check
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        HealthCheck health = influxDBClient.health();
        return ResponseEntity.ok(Map.of(
                "status", health.getStatus().getValue(),
                "message", health.getMessage() != null ? health.getMessage() : "ok"
        ));
    }
}
