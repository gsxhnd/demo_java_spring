package com.example.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppInfoResponse {

    private String displayName;
    private String environmentLabel;
    private String activeProfiles;
    private int cacheTtlSeconds;
    private boolean cacheEnabled;
}
