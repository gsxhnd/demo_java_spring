package com.example.multidb.dto.redis;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Redis 缓存用户请求")
public class CacheUserRequest {

    @NotBlank(message = "缓存键不能为空")
    @Schema(description = "缓存键", example = "user:1001")
    private String key;

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "alice")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "alice@example.com")
    private String email;

    @NotNull(message = "年龄不能为空")
    @Min(value = 0, message = "年龄不能为负数")
    @Schema(description = "年龄", example = "28")
    private Integer age;

    @NotNull(message = "TTL 秒数不能为空")
    @Min(value = 1, message = "TTL 至少为 1 秒")
    @Schema(description = "过期时间（秒）", example = "300")
    private Long ttlSeconds;
}
