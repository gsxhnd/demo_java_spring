package com.example.file.controller;

import com.example.file.service.LocalFileStorageService;
import com.example.file.validator.FileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.*;

/**
 * 文件上传下载控制器
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final LocalFileStorageService fileStorageService;
    private final FileValidator fileValidator;

    // ========== 文件上传 ==========

    /**
     * 单文件上传
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("收到文件上传请求: name={}, size={}", file.getOriginalFilename(), file.getSize());

        // 验证文件
        fileValidator.validate(file);

        // 存储文件
        String storedFilename = fileStorageService.store(file);

        // 生成访问 URL
        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/")
                .path(storedFilename)
                .toUriString();

        Map<String, Object> response = new HashMap<>();
        response.put("filename", storedFilename);
        response.put("originalFilename", file.getOriginalFilename());
        response.put("size", file.getSize());
        response.put("contentType", file.getContentType());
        response.put("url", fileUrl);

        return ResponseEntity.ok(response);
    }

    /**
     * 多文件上传
     */
    @PostMapping("/upload/batch")
    public ResponseEntity<Map<String, Object>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        log.info("收到批量文件上传请求: count={}", files.length);

        List<Map<String, Object>> uploadedFiles = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                fileValidator.validate(file);
                String storedFilename = fileStorageService.store(file);

                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("filename", storedFilename);
                fileInfo.put("originalFilename", file.getOriginalFilename());
                fileInfo.put("size", file.getSize());
                uploadedFiles.add(fileInfo);

            } catch (Exception e) {
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
                log.warn("文件上传失败: {}", file.getOriginalFilename(), e);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("uploaded", uploadedFiles);
        response.put("totalUploaded", uploadedFiles.size());
        response.put("errors", errors);

        return ResponseEntity.ok(response);
    }

    /**
     * 分目录上传
     */
    @PostMapping("/upload/{directory}")
    public ResponseEntity<Map<String, Object>> uploadToDirectory(
            @RequestParam("file") MultipartFile file,
            @PathVariable String directory) {

        log.info("收到文件上传请求: directory={}, name={}", directory, file.getOriginalFilename());

        fileValidator.validate(file);

        String storedFilename = fileStorageService.storeInSubDirectory(file, directory);

        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/")
                .path(storedFilename)
                .toUriString();

        Map<String, Object> response = new HashMap<>();
        response.put("filename", storedFilename);
        response.put("directory", directory);
        response.put("url", fileUrl);

        return ResponseEntity.ok(response);
    }

    // ========== 文件下载 ==========

    /**
     * 下载文件
     */
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename,
                                                   HttpServletRequest request) {
        log.info("收到文件下载请求: filename={}", filename);

        Resource resource;
        try {
            resource = fileStorageService.loadAsResource(filename);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }

        String contentType;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            contentType = "application/octet-stream";
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * 预览文件（inline）
     */
    @GetMapping("/{filename}/preview")
    public ResponseEntity<Resource> previewFile(@PathVariable String filename,
                                                  HttpServletRequest request) {
        log.info("收到文件预览请求: filename={}", filename);

        Resource resource;
        try {
            resource = fileStorageService.loadAsResource(filename);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }

        String contentType;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }

    // ========== 文件管理 ==========

    /**
     * 删除文件
     */
    @DeleteMapping("/{filename}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable String filename) {
        log.info("收到文件删除请求: filename={}", filename);

        boolean deleted = fileStorageService.deleteFile(filename);

        Map<String, Object> response = new HashMap<>();
        response.put("filename", filename);
        response.put("deleted", deleted);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取上传目录信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUploadInfo() {
        Path uploadDir = fileStorageService.getUploadDir();

        Map<String, Object> response = new HashMap<>();
        response.put("uploadDir", uploadDir.toString());
        response.put("absolutePath", uploadDir.toAbsolutePath().toString());

        return ResponseEntity.ok(response);
    }
}
