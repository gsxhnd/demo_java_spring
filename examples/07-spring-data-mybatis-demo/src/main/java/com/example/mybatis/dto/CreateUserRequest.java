package com.example.mybatis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "创建用户请求")
public class CreateUserRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Schema(description = "用户名", example = "john_doe")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "john@example.com")
    private String email;

    @NotNull(message = "年龄不能为空")
    @Min(value = 18, message = "年龄必须大于等于18")
    @Max(value = 150, message = "年龄必须小于等于150")
    @Schema(description = "年龄", example = "30")
    private Integer age;

    @Schema(description = "省份", example = "广东省")
    private String province;

    @Schema(description = "城市", example = "深圳市")
    private String city;

    @Schema(description = "街道", example = "南山区科技园")
    private String street;

    @Schema(description = "邮编", example = "518000")
    private String zipCode;
}
