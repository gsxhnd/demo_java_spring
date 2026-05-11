package com.example.mybatis.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private Long id;
    private String username;
    private String email;
    private Integer age;
    private String status;
    private String province;
    private String city;
    private String street;
    private String zipCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
