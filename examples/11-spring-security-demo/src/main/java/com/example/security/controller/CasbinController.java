package com.example.security.controller;

import com.example.security.dto.AddCasbinPolicyRequest;
import com.example.security.dto.CasbinCheckResponse;
import com.example.security.service.CasbinPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/casbin")
@RequiredArgsConstructor
@Tag(name = "Casbin", description = "RBAC 策略演示（admin 可管理策略）")
@SecurityRequirement(name = "BearerAuth")
public class CasbinController {

    private final CasbinPolicyService casbinPolicyService;

    @GetMapping("/check")
    @Operation(summary = "检查当前用户对资源的权限")
    public ResponseEntity<CasbinCheckResponse> check(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam String resource,
            @RequestParam String action) {
        return ResponseEntity.ok(
                casbinPolicyService.check(principal.getUsername(), resource, action));
    }

    @GetMapping("/permissions")
    @Operation(summary = "查询当前用户的隐式权限列表")
    public ResponseEntity<List<List<String>>> permissions(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(casbinPolicyService.getPoliciesForUser(principal.getUsername()));
    }

    @PostMapping("/policies")
    @Operation(summary = "动态添加 Casbin 策略（需 admin 角色）")
    public ResponseEntity<Map<String, Object>> addPolicy(
            @Valid @RequestBody AddCasbinPolicyRequest request) {
        boolean added = casbinPolicyService.addPolicy(request);
        return ResponseEntity.ok(Map.of(
                "added", added,
                "role", request.getRole(),
                "resource", request.getResource(),
                "action", request.getAction()));
    }
}
