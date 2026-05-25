package com.example.business.service;

import com.example.business.config.StorageProperties;
import com.example.business.dto.FileUploadResponse;
import com.example.business.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final StorageProperties storageProperties;

    public FileUploadResponse store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "unknown");
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("文件名包含非法路径: " + originalFilename);
        }

        Path uploadDir = Paths.get(storageProperties.getUploadDir()).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        String storedFilename = UUID.randomUUID() + "_" + originalFilename;
        Path target = uploadDir.resolve(storedFilename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String downloadUrl = storageProperties.getPublicUrlPrefix() + "/" + storedFilename;
        log.info("文件上传成功 - original: {}, stored: {}", originalFilename, storedFilename);

        return FileUploadResponse.builder()
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .size(file.getSize())
                .downloadUrl(downloadUrl)
                .build();
    }

    public Resource loadAsResource(String filename) throws MalformedURLException {
        Path filePath = Paths.get(storageProperties.getUploadDir())
                .toAbsolutePath()
                .normalize()
                .resolve(filename)
                .normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new ResourceNotFoundException("文件不存在: " + filename);
        }
        return resource;
    }
}
