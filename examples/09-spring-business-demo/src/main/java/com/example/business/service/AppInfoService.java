package com.example.business.service;

import com.example.business.config.AppProperties;
import com.example.business.config.CacheProperties;
import com.example.business.dto.AppInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppInfoService {

    private final AppProperties appProperties;
    private final CacheProperties cacheProperties;
    private final Environment environment;

    public AppInfoResponse getInfo() {
        String profiles = Arrays.stream(environment.getActiveProfiles())
                .collect(Collectors.joining(","));
        if (profiles.isEmpty()) {
            profiles = "default";
        }
        return AppInfoResponse.builder()
                .displayName(appProperties.getDisplayName())
                .environmentLabel(appProperties.getEnvironmentLabel())
                .activeProfiles(profiles)
                .cacheEnabled(cacheProperties.isEnabled())
                .cacheTtlSeconds(cacheProperties.getTtlSeconds())
                .build();
    }
}
