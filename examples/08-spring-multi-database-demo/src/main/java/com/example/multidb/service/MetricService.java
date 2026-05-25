package com.example.multidb.service;

import com.example.multidb.config.InfluxDbProperties;
import com.example.multidb.dto.influx.MetricPointResponse;
import com.example.multidb.dto.influx.WriteMetricRequest;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricService {

    private static final String VALUE_FIELD = "value";

    private final InfluxDBClient influxDBClient;
    private final InfluxDbProperties influxDbProperties;

    public void write(WriteMetricRequest request) {
        Point point = Point.measurement(request.getMeasurement())
                .addField(VALUE_FIELD, request.getValue())
                .time(Instant.now(), WritePrecision.MS);
        if (request.getTags() != null) {
            request.getTags().forEach(point::addTag);
        }
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        writeApi.writePoint(point);
        log.info("InfluxDB 指标写入 - measurement: {}, value: {}",
                request.getMeasurement(), request.getValue());
    }

    public List<MetricPointResponse> query(String measurement, int hours) {
        String flux = """
                from(bucket: "%s")
                  |> range(start: -%dh)
                  |> filter(fn: (r) => r._measurement == "%s")
                  |> filter(fn: (r) => r._field == "%s")
                """.formatted(influxDbProperties.getBucket(), hours, measurement, VALUE_FIELD);

        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(flux, influxDbProperties.getOrg());
        List<MetricPointResponse> results = new ArrayList<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Map<String, String> tags = new HashMap<>();
                record.getValues().forEach((key, val) -> {
                    if (key != null && !key.startsWith("_") && val != null) {
                        tags.put(key, val.toString());
                    }
                });
                Double value = record.getValue() != null
                        ? ((Number) record.getValue()).doubleValue()
                        : null;
                results.add(MetricPointResponse.builder()
                        .time(record.getTime())
                        .measurement(record.getMeasurement())
                        .tags(tags)
                        .field(record.getField())
                        .value(value)
                        .build());
            }
        }
        log.info("InfluxDB 指标查询 - measurement: {}, count: {}", measurement, results.size());
        return results;
    }
}
