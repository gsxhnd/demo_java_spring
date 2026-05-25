package com.example.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50)
    private String password;

    @Pattern(regexp = "user", message = "演示环境仅允许注册 user 角色")
    private String role = "user";
}
