# 文件上传与下载 — File Upload & Download

> MultipartFile, 大文件流式处理, MinIO/S3 集成, 安全校验

## 1. 概述 / Overview

文件上传/下载是 Web 应用的常见需求。Spring Boot 通过 `MultipartFile` 提供开箱即用的文件上传支持，结合流式处理和对象存储可应对各种生产场景。

### 存储方案对比

| 方案 | 适用场景 | 优点 | 缺点 |
|---|---|---|---|
| 本地磁盘 | 开发/小型项目 | 简单直接 | 不支持集群、无冗余 |
| MinIO | 私有化部署 | S3 兼容、免费开源 | 需自行运维 |
| AWS S3 / 阿里云 OSS | 生产环境 | 高可用、CDN 集成 | 按量付费 |
| 数据库 BLOB | 小文件/证书 | 事务一致性 | 性能差、占用 DB 空间 |

---

## 2. 核心概念 / Core Concepts

### Spring Boot 文件上传机制

```
Client (multipart/form-data)
     │
     ▼
┌─────────────────────────────────┐
│  MultipartResolver              │
│  (StandardServletMultipart)     │
│                                 │
│  解析 multipart 请求             │
│  生成临时文件                    │
└─────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────┐
│  Controller                     │
│                                 │
│  @RequestParam MultipartFile    │
│  获取文件名、大小、流             │
└─────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────┐
│  Storage Service                │
│                                 │
│  本地磁盘 / MinIO / S3          │
└─────────────────────────────────┘
```

### 关键配置项

| 配置 | 默认值 | 说明 |
|---|---|---|
| `spring.servlet.multipart.enabled` | `true` | 启用 multipart 支持 |
| `spring.servlet.multipart.max-file-size` | `1MB` | 单个文件最大大小 |
| `spring.servlet.multipart.max-request-size` | `10MB` | 整个请求最大大小 |
| `spring.servlet.multipart.file-size-threshold` | `0B` | 超过此值写入临时文件 |
| `spring.servlet.multipart.location` | 系统临时目录 | 临时文件存储路径 |

---

## 3. 快速开始 / Quick Start

### 3.1 配置文件大小限制

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 100MB
      file-size-threshold: 2MB
```

### 3.2 文件上传 Controller

```java
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService storageService;

    public FileController(FileStorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * 单文件上传
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file) {

        String fileUrl = storageService.store(file);
        return ResponseEntity.ok(Map.of(
            "url", fileUrl,
            "filename", file.getOriginalFilename(),
            "size", String.valueOf(file.getSize())
        ));
    }

    /**
     * 多文件上传
     */
    @PostMapping("/upload/batch")
    public ResponseEntity<List<String>> uploadBatch(
            @RequestParam("files") List<MultipartFile> files) {

        List<String> urls = files.stream()
            .map(storageService::store)
            .toList();
        return ResponseEntity.ok(urls);
    }
}
```

### 3.3 本地存储实现

```java
@Service
public class LocalFileStorageService implements FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public String store(MultipartFile file) {
        try {
            Path uploadPath = Path.of(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名，防止覆盖
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path target = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return "/files/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("文件存储失败", e);
        }
    }
}
```

### 3.4 文件下载

```java
@GetMapping("/download/{filename}")
public ResponseEntity<Resource> download(@PathVariable String filename) {
    Path filePath = Path.of(uploadDir).resolve(filename);
    Resource resource = new UrlResource(filePath.toUri());

    if (!resource.exists()) {
        return ResponseEntity.notFound().build();
    }

    String contentType = Files.probeContentType(filePath);
    if (contentType == null) {
        contentType = "application/octet-stream";
    }

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + filename + "\"")
        .body(resource);
}
```

---

## 4. 进阶用法 / Advanced Usage

### 4.1 文件类型校验

```java
public class FileValidator {

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif",
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private static final long MAX_SIZE = 50 * 1024 * 1024; // 50MB

    public static void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("文件大小超过限制");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("不支持的文件类型: " + file.getContentType());
        }

        // 通过文件头（Magic Number）二次校验，防止伪造 Content-Type
        validateMagicNumber(file);
    }

    private static void validateMagicNumber(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            is.read(header);

            // JPEG: FF D8 FF
            // PNG: 89 50 4E 47
            // PDF: 25 50 44 46
            if (!matchesExpectedFormat(header, file.getContentType())) {
                throw new IllegalArgumentException("文件内容与声明类型不匹配");
            }
        } catch (IOException e) {
            throw new RuntimeException("文件校验失败", e);
        }
    }
}
```

### 4.2 大文件流式上传

对于大文件，避免将整个文件加载到内存：

```java
@PostMapping("/upload/stream")
public ResponseEntity<String> streamUpload(HttpServletRequest request)
        throws IOException, ServletException {

    // 直接从 request 获取 Part，避免 MultipartFile 的内存缓冲
    Part filePart = request.getPart("file");

    Path target = Path.of(uploadDir, UUID.randomUUID() + "_" + filePart.getSubmittedFileName());
    try (InputStream is = filePart.getInputStream();
         OutputStream os = Files.newOutputStream(target)) {
        is.transferTo(os);
    }

    return ResponseEntity.ok(target.getFileName().toString());
}
```

### 4.3 MinIO 集成

```xml
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.13</version>
</dependency>
```

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: my-app
```

```java
@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build();
    }
}
```

```java
@Service
public class MinioStorageService implements FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public MinioStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public String store(MultipartFile file) {
        try {
            String objectName = UUID.randomUUID() + "/" + file.getOriginalFilename();

            minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());

            return "/" + bucket + "/" + objectName;
        } catch (Exception e) {
            throw new RuntimeException("MinIO 上传失败", e);
        }
    }

    public InputStream download(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build());
        } catch (Exception e) {
            throw new RuntimeException("MinIO 下载失败", e);
        }
    }
}
```

### 4.4 存储策略抽象

```java
public interface FileStorageService {
    String store(MultipartFile file);
    InputStream load(String path);
    void delete(String path);
}
```

通过 `@Profile` 或配置切换实现：

```java
@Service
@Profile("local")
public class LocalFileStorageService implements FileStorageService { ... }

@Service
@Profile("minio")
public class MinioStorageService implements FileStorageService { ... }
```

---

## 5. 常见问题 / FAQ

### Q1: 上传时报 `MaxUploadSizeExceededException`

配置 `spring.servlet.multipart.max-file-size` 和 `max-request-size`。如果使用 Nginx 反向代理，还需配置 `client_max_body_size`。

### Q2: 临时文件目录不存在导致上传失败

Spring Boot 使用系统临时目录存放上传的临时文件，某些 Linux 系统会定期清理 `/tmp`。解决方案：

```yaml
spring:
  servlet:
    multipart:
      location: /var/app/tmp
```

### Q3: 文件名包含中文或特殊字符

下载时对文件名进行 URL 编码：

```java
String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
    .replace("+", "%20");
return ResponseEntity.ok()
    .header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename*=UTF-8''" + encodedFilename)
    .body(resource);
```

### Q4: 如何限制上传文件类型？

不要仅依赖 `Content-Type`（客户端可伪造），应结合文件头 Magic Number 校验。参见 4.1 节。

### Q5: 集群环境下本地存储的问题

本地存储在多实例部署时无法共享文件。解决方案：
- 使用 MinIO / S3 等对象存储
- 使用 NFS 共享挂载
- 使用 CDN + 对象存储

---

## 6. 示例项目 / Example

完整可运行代码见 → [`examples/spring-file-demo/`](../../examples/spring-file-demo/)

**演示功能：**
- 单文件上传
- 多文件批量上传
- 分目录存储
- 文件下载
- 文件预览
- 文件类型验证（MIME Type + Magic Number）
- 文件大小限制

**运行示例：**
```bash
cd examples/spring-file-demo
mvn spring-boot:run
```

**API 接口：**
- `POST /api/files/upload` - 单文件上传
- `POST /api/files/upload/batch` - 批量上传
- `GET /api/files/{filename}` - 文件下载
- `GET /api/files/{filename}/preview` - 文件预览
- `DELETE /api/files/{filename}` - 删除文件

---

## 7. 参考资料 / References

- [Spring Boot File Upload](https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.spring-mvc.multipart)
- [MinIO Java Client](https://min.io/docs/minio/linux/developers/java/minio-java.html)
- [AWS S3 SDK for Java](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/java_s3_code_examples.html)
