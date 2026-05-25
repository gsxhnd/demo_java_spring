package com.example.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddCasbinPolicyRequest {

    @NotBlank
    private String role;

    @NotBlank
    private String resource;

    @NotBlank
    private String action;
}
