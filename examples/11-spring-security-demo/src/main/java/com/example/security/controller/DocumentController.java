package com.example.security.controller;

import com.example.security.dto.CreateDocumentRequest;
import com.example.security.dto.DocumentResponse;
import com.example.security.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "文档", description = "Casbin 控制：user 可 GET/POST，admin 全部操作")
@SecurityRequirement(name = "BearerAuth")
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    @Operation(summary = "文档列表（Casbin: GET /api/documents）")
    public ResponseEntity<List<DocumentResponse>> list() {
        return ResponseEntity.ok(documentService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "文档详情（Casbin: GET /api/documents/*）")
    public ResponseEntity<DocumentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.findById(id));
    }

    @PostMapping
    @Operation(summary = "创建文档（Casbin: POST /api/documents）")
    public ResponseEntity<DocumentResponse> create(
            @Valid @RequestBody CreateDocumentRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return new ResponseEntity<>(
                documentService.create(request, principal.getUsername()),
                HttpStatus.CREATED);
    }
}
