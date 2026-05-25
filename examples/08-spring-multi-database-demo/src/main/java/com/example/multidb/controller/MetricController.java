package com.example.multidb.controller;

import com.example.multidb.dto.influx.MetricPointResponse;
import com.example.multidb.dto.influx.WriteMetricRequest;
import com.example.multidb.service.MetricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/influx/metrics")
@RequiredArgsConstructor
@Validated
@Tag(name = "InfluxDB", description = "InfluxDB 时序监控指标")
public class MetricController {

    private final MetricService metricService;

    @PostMapping
    @Operation(summary = "写入时序指标点")
    public ResponseEntity<Void> write(@Valid @RequestBody WriteMetricRequest request) {
        metricService.write(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @Operation(summary = "查询最近时序指标（Flux）")
    public ResponseEntity<List<MetricPointResponse>> query(
            @Parameter(description = "指标名") @RequestParam String measurement,
            @Parameter(description = "查询最近多少小时") @RequestParam(defaultValue = "1")
            @Min(1) @Max(168) int hours) {
        return ResponseEntity.ok(metricService.query(measurement, hours));
    }
}
