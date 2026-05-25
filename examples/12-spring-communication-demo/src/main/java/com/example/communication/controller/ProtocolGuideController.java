package com.example.communication.controller;

import com.example.communication.dto.ProtocolMatrixRow;
import com.example.communication.dto.ProtocolRecommendRequest;
import com.example.communication.dto.ProtocolRecommendResponse;
import com.example.communication.service.ProtocolGuideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/protocols")
@RequiredArgsConstructor
public class ProtocolGuideController {

    private final ProtocolGuideService protocolGuideService;

    @GetMapping("/matrix")
    public ResponseEntity<List<ProtocolMatrixRow>> matrix() {
        return ResponseEntity.ok(protocolGuideService.matrix());
    }

    @PostMapping("/recommend")
    public ResponseEntity<ProtocolRecommendResponse> recommend(
            @Valid @RequestBody ProtocolRecommendRequest request) {
        return ResponseEntity.ok(protocolGuideService.recommend(request));
    }

    @GetMapping("/scenarios")
    public ResponseEntity<List<Map<String, String>>> scenarios() {
        List<Map<String, String>> items = List.of(
                Map.of("code", "WEB_API", "label", "通用 Web API"),
                Map.of("code", "MICROSERVICE_INTERNAL", "label", "微服务内部调用"),
                Map.of("code", "BROWSER_REALTIME", "label", "浏览器实时推送"),
                Map.of("code", "IOT_SENSORS", "label", "IoT 传感器采集"),
                Map.of("code", "INDUSTRIAL_PLC", "label", "工业 PLC 直连"),
                Map.of("code", "LARGE_FILE_TRANSFER", "label", "大文件传输"));
        return ResponseEntity.ok(items);
    }
}
