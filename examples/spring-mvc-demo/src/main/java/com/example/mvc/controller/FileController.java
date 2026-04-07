package com.example.mvc.controller;

import com.example.mvc.dto.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件操作控制器
 *
 * 演示：
 * - 文件上传
 * - 文件下载
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final String UPLOAD_DIR = System.getProperty("java.io.tmpdir") + "/uploads/";

    public FileController() {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录", e);
        }
    }

    /**
     * 单文件上传
     *
     * @RequestParam("file") 指定表单字段名
     */
    @PostMapping("/upload")
    public ApiResponse<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ApiResponse.error("请选择要上传的文件");
        }

        String originalFilename = file.getOriginalFilename();
        String storedFilename = System.currentTimeMillis() + "_" + originalFilename;
        Path targetPath = Paths.get(UPLOAD_DIR, storedFilename);

        file.transferTo(targetPath);

        Map<String, String> result = new HashMap<>();
        result.put("originalName", originalFilename);
        result.put("storedName", storedFilename);
        result.put("size", String.valueOf(file.getSize()));
        result.put("path", targetPath.toString());

        return ApiResponse.success("文件上传成功", result);
    }

    /**
     * 多文件上传
     */
    @PostMapping("/upload/multiple")
    public ApiResponse<Map<String, Object>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files) throws IOException {
        if (files.length == 0) {
            return ApiResponse.error("请选择要上传的文件");
        }

        Map<String, Object> results = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String storedFilename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                file.transferTo(Paths.get(UPLOAD_DIR, storedFilename));
                successCount++;
            } else {
                failCount++;
            }
        }

        results.put("successCount", successCount);
        results.put("failCount", failCount);
        results.put("totalFiles", files.length);

        return ApiResponse.success("上传完成", results);
    }

    /**
     * 文件下载
     */
    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get(UPLOAD_DIR, filename);

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        byte[] content = Files.readAllBytes(filePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(content.length);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/info/{filename}")
    public ApiResponse<Map<String, Object>> getFileInfo(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get(UPLOAD_DIR, filename);

        if (!Files.exists(filePath)) {
            return ApiResponse.error("文件不存在");
        }

        Map<String, Object> info = new HashMap<>();
        info.put("filename", filename);
        info.put("size", Files.size(filePath));
        info.put("lastModified", Files.getLastModifiedTime(filePath).toMillis());

        return ApiResponse.success(info);
    }
}
