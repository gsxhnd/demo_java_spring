package com.example.mybatis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "错误响应")
public class ErrorResponse {

    @Schema(description = "错误代码")
    private int code;

    @Schema(description = "错误消息")
    private String message;

    @Schema(description = "详细信息")
    private String detail;

    @Schema(description = "请求路径")
    private String path;

    @Schema(description = "时间戳")
    private LocalDateTime timestamp;
}
