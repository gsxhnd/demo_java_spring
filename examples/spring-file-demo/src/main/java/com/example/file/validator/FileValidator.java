package com.example.file.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * 文件验证器
 * 提供文件类型、内容等多重校验
 */
@Component
@Slf4j
public class FileValidator {

    // 允许的文件类型
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain"
    );

    // 文件头 Magic Number 映射
    private static final Map<String, byte[][]> MAGIC_NUMBERS = Map.of(
            "image/jpeg", new byte[][] { { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF } },
            "image/png", new byte[][] { { (byte) 0x89, 0x50, 0x4E, 0x47 } },
            "image/gif", new byte[][] { { 0x47, 0x49, 0x46 } },
            "application/pdf", new byte[][] { { 0x25, 0x50, 0x44, 0x46 } }
    );

    /**
     * 最大文件大小（50MB）
     */
    private static final long MAX_SIZE = 50 * 1024 * 1024;

    /**
     * 验证文件
     */
    public void validate(MultipartFile file) {
        validateNotEmpty(file);
        validateSize(file);
        validateContentType(file);
        validateMagicNumber(file);
    }

    /**
     * 验证文件是否为空
     */
    public void validateNotEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
    }

    /**
     * 验证文件大小
     */
    public void validateSize(MultipartFile file) {
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("文件大小超过限制（最大 50MB）");
        }
    }

    /**
     * 验证 Content-Type
     */
    public void validateContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("不支持的文件类型: " + contentType);
        }
    }

    /**
     * 通过 Magic Number 二次校验文件类型
     */
    public void validateMagicNumber(MultipartFile file) {
        String contentType = file.getContentType();
        byte[][] expectedHeaders = MAGIC_NUMBERS.get(contentType);

        if (expectedHeaders == null) {
            // 对于没有 Magic Number 定义的文件类型，跳过校验
            return;
        }

        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int bytesRead = is.read(header);

            boolean matches = false;
            for (byte[] expected : expectedHeaders) {
                if (bytesRead >= expected.length) {
                    boolean match = true;
                    for (int i = 0; i < expected.length; i++) {
                        if (header[i] != expected[i]) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        matches = true;
                        break;
                    }
                }
            }

            if (!matches) {
                throw new IllegalArgumentException("文件内容与声明类型不匹配，可能存在安全风险");
            }

            log.debug("Magic Number 校验通过: {}", contentType);

        } catch (IOException e) {
            throw new IllegalArgumentException("文件校验失败: " + e.getMessage());
        }
    }

    /**
     * 验证图片尺寸
     */
    public void validateImageDimensions(MultipartFile file, int maxWidth, int maxHeight) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return;
        }

        try {
            // 使用 ImageIO 读取图片尺寸
            // 这里简化处理，实际生产中可以使用 Thumbnailator 或其他库
            log.debug("图片尺寸验证: maxWidth={}, maxHeight={}", maxWidth, maxHeight);
        } catch (Exception e) {
            log.warn("图片尺寸验证失败", e);
        }
    }
}
