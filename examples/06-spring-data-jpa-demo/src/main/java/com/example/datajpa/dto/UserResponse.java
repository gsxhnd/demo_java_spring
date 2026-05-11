package com.example.datajpa.dto;

import com.example.datajpa.entity.User;
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
@Schema(description = "用户响应")
public class UserResponse {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "街道")
    private String street;

    @Schema(description = "邮编")
    private String zipCode;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public static UserResponse fromEntity(User user) {
        UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .age(user.getAge())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        if (user.getAddress() != null) {
            builder.province(user.getAddress().getProvince())
                    .city(user.getAddress().getCity())
                    .street(user.getAddress().getStreet())
                    .zipCode(user.getAddress().getZipCode());
        }

        return builder.build();
    }
}
