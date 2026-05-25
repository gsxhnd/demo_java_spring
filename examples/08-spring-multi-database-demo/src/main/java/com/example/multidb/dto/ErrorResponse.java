package com.example.multidb.dto;

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

    private int code;
    private String message;
    private String detail;
    private String path;
    private LocalDateTime timestamp;
}
