package com.example.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "管理", description = "仅 admin 角色可访问（Casbin: /api/admin/**）")
@SecurityRequirement(name = "BearerAuth")
public class AdminController {

    @GetMapping("/dashboard")
    @Operation(summary = "管理面板（Casbin 授权）")
    public ResponseEntity<Map<String, Object>> dashboard(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(Map.of(
                "message", "欢迎管理员",
                "username", principal.getUsername(),
                "authorities", principal.getAuthorities()));
    }
}
