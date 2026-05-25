package com.example.multidb.dto.mongo;

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
@Schema(description = "创建用户行为日志请求")
public class CreateUserLogRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @NotBlank(message = "行为不能为空")
    @Schema(description = "行为", example = "LOGIN")
    private String action;

    @Schema(description = "扩展详情（灵活 Schema）")
    private Map<String, Object> details;
}
