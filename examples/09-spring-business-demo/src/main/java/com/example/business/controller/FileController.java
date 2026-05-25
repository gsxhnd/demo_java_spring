package com.example.business.controller;

import com.example.business.dto.FileUploadResponse;
import com.example.business.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "文件", description = "MultipartFile 上传与 Resource 下载")
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文件")
    public ResponseEntity<FileUploadResponse> upload(@RequestParam("file") MultipartFile file)
            throws IOException {
        return ResponseEntity.ok(fileStorageService.store(file));
    }

    @GetMapping("/download/{filename}")
    @Operation(summary = "下载文件（Resource 流式响应）")
    public ResponseEntity<Resource> download(@PathVariable String filename) throws MalformedURLException {
        Resource resource = fileStorageService.loadAsResource(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
