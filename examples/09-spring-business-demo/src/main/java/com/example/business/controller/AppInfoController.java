package com.example.business.controller;

import com.example.business.dto.AppInfoResponse;
import com.example.business.dto.ScheduledTaskStatusResponse;
import com.example.business.service.AppInfoService;
import com.example.business.service.ScheduledMaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
@Tag(name = "应用信息", description = "@ConfigurationProperties 与 Profile")
public class AppInfoController {

    private final AppInfoService appInfoService;
    private final ScheduledMaintenanceService scheduledMaintenanceService;

    @GetMapping("/info")
    @Operation(summary = "查看当前环境与配置（Profile + ConfigurationProperties）")
    public ResponseEntity<AppInfoResponse> info() {
        return ResponseEntity.ok(appInfoService.getInfo());
    }

    @GetMapping("/scheduled-status")
    @Operation(summary = "查看定时任务最近执行状态（@Scheduled）")
    public ResponseEntity<ScheduledTaskStatusResponse> scheduledStatus() {
        return ResponseEntity.ok(scheduledMaintenanceService.getStatus());
    }
}
