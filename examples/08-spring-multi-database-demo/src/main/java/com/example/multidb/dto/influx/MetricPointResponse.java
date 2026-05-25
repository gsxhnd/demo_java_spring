package com.example.multidb.dto.influx;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "时序指标数据点")
public class MetricPointResponse {

    private Instant time;
    private String measurement;
    private Map<String, String> tags;
    private String field;
    private Double value;
}
