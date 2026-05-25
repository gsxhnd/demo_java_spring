package com.example.communication.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProtocolRecommendResponse {

    private String scenario;
    private String recommendedProtocol;
    private String rationale;
    private List<String> alternatives;
    private String demoEndpoint;
}
