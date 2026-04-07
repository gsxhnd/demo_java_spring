package com.example.file.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * 本地文件存储服务
 */
@Service
@Slf4j
public class LocalFileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.allowed-extensions}")
    private Set<String> allowedExtensions;

    @Value("${file.allowed-content-types}")
    private Set<String> allowedContentTypes;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootLocation);
            log.info("文件上传目录初始化: {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("无法创建文件上传目录", e);
        }
    }

    /**
     * 存储文件
     *
     * @param file 上传的文件
     * @return 存储的文件名
     */
    public String store(MultipartFile file) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // 检查文件名是否安全
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("文件名包含非法路径序列: " + originalFilename);
        }

        // 生成唯一文件名
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + extension;

        try (InputStream inputStream = file.getInputStream()) {
            Path targetLocation = rootLocation.resolve(uniqueFilename);
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("文件存储成功: original={}, stored={}", originalFilename, uniqueFilename);
            return uniqueFilename;

        } catch (IOException e) {
            throw new RuntimeException("文件存储失败: " + originalFilename, e);
        }
    }

    /**
     * 存储文件到子目录
     */
    public String storeInSubDirectory(MultipartFile file, String subDirectory) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // 创建子目录
        Path subDir = rootLocation.resolve(subDirectory);
        try {
            Files.createDirectories(subDir);
        } catch (IOException e) {
            throw new RuntimeException("无法创建子目录: " + subDirectory, e);
        }

        // 生成唯一文件名
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + extension;

        try (InputStream inputStream = file.getInputStream()) {
            Path targetLocation = subDir.resolve(uniqueFilename);
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("文件存储成功: subDir={}, stored={}", subDirectory, uniqueFilename);
            return subDirectory + "/" + uniqueFilename;

        } catch (IOException e) {
            throw new RuntimeException("文件存储失败: " + originalFilename, e);
        }
    }

    /**
     * 加载文件为 Resource
     */
    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("无法读取文件: " + filename);
            }

        } catch (MalformedURLException e) {
            throw new RuntimeException("文件路径无效: " + filename, e);
        }
    }

    /**
     * 获取文件路径
     */
    public Path getFilePath(String filename) {
        return rootLocation.resolve(filename);
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            boolean deleted = Files.deleteIfExists(file);
            if (deleted) {
                log.info("文件删除成功: {}", filename);
            }
            return deleted;
        } catch (IOException e) {
            log.error("文件删除失败: {}", filename, e);
            return false;
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件大小（已在配置中通过 max-file-size 限制）

        // 检查文件扩展名
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }

        // 检查 Content-Type
        String contentType = file.getContentType();
        if (contentType != null && !allowedContentTypes.contains(contentType)) {
            throw new IllegalArgumentException("不支持的 MIME 类型: " + contentType);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1) : "";
    }

    /**
     * 获取上传目录
     */
    public Path getUploadDir() {
        return rootLocation;
    }
}
