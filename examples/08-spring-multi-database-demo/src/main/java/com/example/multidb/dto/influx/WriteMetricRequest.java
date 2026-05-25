package com.example.multidb.dto.influx;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "写入时序指标请求")
public class WriteMetricRequest {

    @NotBlank(message = "measurement 不能为空")
    @Schema(description = "指标名", example = "request_latency")
    private String measurement;

    @Schema(description = "标签", example = "{\"host\":\"server-1\",\"endpoint\":\"/api/users\"}")
    private Map<String, String> tags;

    @NotNull(message = "value 不能为空")
    @Schema(description = "指标值", example = "42.5")
    private Double value;
}
