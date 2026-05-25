package com.example.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CasbinCheckResponse {

    private String username;
    private String resource;
    private String action;
    private boolean allowed;
}
