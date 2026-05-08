---
title: Java Spring 文件处理
created: 2026-05-08 22:29:19
category: Java-Spring
tags:
  - Java
  - Spring
  - 文件上传
  - 文件下载
  - MultipartFile
  - 对象存储
---

<!-- markdownlint-disable MD025 -->

# Java Spring 文件处理

## 为什么要学文件处理

到目前为止我们处理的数据都是结构化的文本 — JSON 请求体、数据库记录、配置参数。但现实世界的应用远不止这些：用户要上传头像、运营要导入 Excel 报表、前端要下载 PDF 合同、日志文件要打包归档... 这些场景处理的是非结构化的二进制数据，与处理 JSON 请求体有本质区别：数据量大、不适合一次性加载到内存、需要考虑存储策略（本地磁盘 vs 对象存储）、需要控制文件大小和类型防止滥用。

Spring Boot 对文件处理提供了良好的支持，`MultipartFile` 抽象了文件上传，`Resource` 抽象了文件下载。理解这两个抽象以及背后的流式处理思想，是完成业务能力闭环的最后一块拼图。

## 核心概念

### MultipartFile 是什么

`MultipartFile` 是 Spring 对 HTTP multipart/form-data 请求中文件数据的抽象。它封装了上传文件的原始字节、文件名、Content-Type 等信息，让你不需要手动解析 HTTP 请求中的 `boundary` 分隔符和二进制区块。

**换个说法：** 前端通过 `<input type="file">` 上传的文件，浏览器用 multipart 格式打包发送。`MultipartFile` 就是帮你拆开这个快递包裹的工具 — 它直接给你文件内容和元信息，你不需要关心包裹是怎么打包的。

### 为什么需要 MultipartFile

在没有框架帮助的情况下，解析 multipart 请求需要手动处理字节流、识别 boundary 标记、提取文件名和 Content-Type、将文件内容与表单字段分离。这是 HTTP 协议层次的脏活。

`MultipartFile` 把这些底层细节完全封装，你只需要用 `file.getBytes()` 或 `file.getInputStream()` 就能拿到文件数据。

### 没有 MultipartFile 会怎样

**困境：** 用原始的 `HttpServletRequest.getInputStream()` 然后自己写 multipart 解析器，代码量 100+ 行，还容易搞错边界条件导致文件损坏。而且每次都要重复写。

**有了 MultipartFile 之后：** Controller 方法参数直接声明 `@RequestParam("file") MultipartFile file`，Spring 自动完成解析和注入。

### Resource 是什么

`Resource` 是 Spring 对底层资源（文件、classpath 资源、URL 资源等）的统一抽象接口。通过 `Resource`，你可以用相同的 API 读取来自不同来源的数据 — 本地文件、JAR 包内的资源、HTTP 远程文件，甚至内存中的字节数组。

**换个说法：** `Resource` 就像是统一的"文件句柄" — 不管文件存在哪，用同一个接口打开、读取、关闭。你不需要分别记住"读本地文件用 FileInputStream，读 JAR 内文件用 getResourceAsStream"。

### 为什么需要 Resource

在文件下载场景中，你需要把文件以流的形式返回给客户端。不同来源的文件（磁盘、classpath、云存储）有不同的读取方式，如果硬编码 `new FileInputStream(path)`，你的代码就和本地文件系统绑死了。

`Resource` 接口统一了 `getInputStream()`、`getFile()`、`contentLength()` 等操作，配合 `ResourceHttpMessageConverter`，可以直接把 `Resource` 作为 Controller 返回值，Spring 自动处理流式输出。

### 没有 Resource 会怎样

**困境：** 对于不同来源的文件，你需要写不同的读取和输出代码。文件从本地迁移到 OSS 后，所有下载代码都要重写。

**有了 Resource 之后：** 提供者实现 `Resource` 接口，消费者只依赖接口。文件存储位置变更只需要替换 Resource 实现，Controller 代码不变。

## 概念深入解释

### 文件上传流程

```
HTTP POST multipart/form-data
    │
    ▼
DispatcherServlet
    │
    ▼
MultipartResolver (StandardServletMultipartResolver)
    │  解析 multipart 请求，提取文件
    ▼
Controller (@RequestParam MultipartFile file)
    │
    ▼
Service (file.getBytes() / file.transferTo())
    │
    ▼
存储层 (本地文件系统 / OSS / MinIO)
```

**上传配置（application.yml）：**

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB        # 单个文件最大
      max-request-size: 50MB     # 整个请求最大（含表单其他字段）
```

**Controller 接收：**

```java
@PostMapping("/upload")
public ResponseEntity<String> upload(
        @RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
        return ResponseEntity.badRequest().body("File is empty");
    }
    String savedPath = storageService.store(file);
    return ResponseEntity.ok(savedPath);
}
```

### MultipartFile 核心方法

| 方法 | 用途 | 注意 |
|------|------|------|
| `getOriginalFilename()` | 获取原始文件名 | 不可信，用户可能上传恶意文件名 |
| `getContentType()` | 获取 MIME 类型 | 不可信，可以伪造 |
| `getSize()` | 获取文件大小（字节） | 用于限制文件大小 |
| `getBytes()` | 一次性读取全部字节 | **小文件专用**，大文件会 OOM |
| `getInputStream()` | 获取输入流 | **大文件推荐**，流式读取 |
| `transferTo(File dest)` | 直接转存到本地文件 | 底层可能用零拷贝优化 |

### 文件下载流程

```
请求 GET /files/{id}
    │
    ▼
Controller → Service (加载 Resource)
    │
    ▼
返回 ResponseEntity<Resource>
    │  设置 Content-Type、Content-Disposition、Content-Length
    ▼
Spring ResourceHttpMessageConverter
    │  流式写入响应体
    ▼
客户端接收文件
```

**Controller 下载示例：**

```java
@GetMapping("/download/{fileId}")
public ResponseEntity<Resource> download(@PathVariable String fileId) {
    FileResource resource = fileService.loadAsResource(fileId);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + resource.getFilename() + "\"")
        .body(resource);
}
```

### 存储策略对比

| 策略 | 优点 | 缺点 | 适用场景 |
|------|------|------|----------|
| 本地磁盘 | 简单、零运维、低延迟 | 单机瓶颈、磁盘故障丢数据、扩展难 | 开发环境、小文件、单实例 |
| 对象存储 (OSS/S3/MinIO) | 无限扩展、高可用、CDN 加速 | 有网络延迟、有费用、需要集成 SDK | 生产环境、大规模文件、多实例 |
| 数据库 BLOB | 事务一致、备份简单 | 严重拖慢数据库、成本高 | **不推荐**，除非有强事务需求 |

### 文件存储 Service 设计

```java
public interface FileStorageService {
    String store(MultipartFile file);
    Resource loadAsResource(String fileId);
    void delete(String fileId);
}
```

接口定义后，`LocalFileStorageService` 和 `OssFileStorageService` 分别实现。切换存储策略只需要改配置，不修改任何调用方代码。这是策略模式（Strategy Pattern）在文件存储场景中的典型应用。

### 安全注意事项

- **文件类型校验不要只信 Content-Type：** 它由客户端发送，可以伪造。应该检测文件魔数（magic number），比如 PNG 文件头是 `89 50 4E 47`。
- **文件名重构：** 用户上传的文件名可能包含路径穿越字符（`../../etc/passwd`），存储时用 MD5 或 UUID 重命名。
- **上传目录不可直接访问：** 上传的文件不应放在 Web 可访问的静态资源目录下，防止被直接 URL 访问或执行（如上传 JSP 文件）。
- **流式处理大文件：** 几百 MB 的文件不能用 `getBytes()` 一次性读入内存，要用流式处理 — `transferTo()` 或 `getInputStream()`。

## 核心要点

1. **小文件用 getBytes，大文件用 getInputStream 或 transferTo：** 避免将大文件一次性加载到内存导致 OOM。
2. **上传限制在配置层控制：** `spring.servlet.multipart.max-file-size` 在框架层拦截超大文件，比在 Service 里手工判断更早更安全。
3. **存储逻辑抽象为接口：** 用 `FileStorageService` 接口隔离调用方和存储实现，切换本地/OSS 只需改实现类。
4. **返回下载文件用 ResponseEntity<Resource>：** Spring 的 `ResourceHttpMessageConverter` 自动处理流式输出。
5. **文件名安全处理：** 用户上传的文件名不可信，存储时用 UUID 重命名，保留原始文件名在数据库中做映射。

## 常见误区

- **上传大文件时 Controller 返回 413 错误。** 这是 `max-file-size` 或 `max-request-size` 配置被触发。检查 `application.yml` 中的配置值，注意 `max-request-size` 需要大于等于 `max-file-size`（因为请求还包含其他表单数据）。
- **下载大文件时服务器内存飙升。** 如果用了 `Resource` 的 `getFile()` 方法返回文件，Spring 可能一次性把整个文件加载到内存。推荐使用 `InputStreamResource` 配合流式输出，确保数据边读边写。
- **文件上传后无法访问，返回 404。** 文件存储在了 Spring Boot 默认的临时目录，重启后被清理。生产环境必须把文件存到持久化目录或对象存储中。
- **文件名包含中文导致下载乱码。** `Content-Disposition` 头需要正确编码。使用 `URLEncoder.encode(filename, StandardCharsets.UTF_8)` 处理非 ASCII 字符。
- **直接用用户上传的文件名存储文件。** 存在路径穿越攻击风险（`../../../etc/passwd`）、文件名冲突风险（两个用户上传同名文件）、特殊字符问题（空格、Unicode）。统一用 UUID 重命名。
- **认为 MultipartFile.transferTo 是零成本操作。** `transferTo` 对某些实现（如 `StandardMultipartFile`）确实是文件系统间的移动/复制，但对 `CommonsMultipartFile` 则是流式复制。不要假设它是零拷贝。

## 与其他概念的关联

- **前置：** [Java Spring Controller](./16_Java%20Spring%20Controller.md) -- 文件上传下载的入口在 Controller 层
- **前置：** [Java Spring 请求处理](./17_Java%20Spring%20请求处理.md) -- `@RequestParam` 接收 MultipartFile
- **前置：** [Java Spring 异常处理](./19_Java%20Spring%20异常处理.md) -- 文件太大、类型不符等异常需要全局异常处理器统一处理
- **前置：** [Java Spring Boot 配置](./14_Java%20Spring%20Boot%20配置.md) -- 文件大小限制在 `application.yml` 中配置
- **并行：** [Java Spring Service 层](./30_Java%20Spring%20Service%20层.md) -- 文件存储逻辑封装为 `FileStorageService`
- **后续：** [Java Spring Cloud Gateway](../Spring_Cloud/Java Spring Cloud Gateway.md) -- 使用 Gateway 统一处理文件上传，设置统一的文件大小限制
