package com.example.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一错误响应格式 — 全局异常处理器返回给客户端的标准错误结构。
 *
 * <h2>为什么需要统一错误响应格式？</h2>
 * <p>在没有统一格式的情况下，不同类型的异常可能返回不同的 JSON 结构（或甚至 HTML 错误页），
 * 客户端（前端/第三方调用方）需要为每种错误写不同的解析逻辑，这非常繁琐且容易出错。
 * 统一的 ErrorResponse 确保了<b>所有 API 错误以一致的 JSON 结构返回</b>，前端只需一种解析方式。</p>
 *
 * <h2>典型响应示例（JSON）</h2>
 * <pre>{@code
 * {
 *   "code": 404,
 *   "message": "用户不存在: abc123",
 *   "detail": "请求的用户资源不存在",
 *   "path": "/api/users/abc123",
 *   "timestamp": "2026-05-05T13:53:57"
 * }
 * }</pre>
 *
 * <h2>各字段的设计意图</h2>
 * <ul>
 *   <li><b>code</b> — HTTP 状态码的数字形式（如 400、404、500）。
 *   即使某些客户端无法读取 HTTP 响应头中的 status code，仍可从 JSON body 中获取错误类型。</li>
 *   <li><b>message</b> — 面向用户的简短错误描述（如"用户不存在"），
 *   适合直接展示在 UI 上。注意不要在此字段中暴露内部技术细节（如堆栈跟踪）。</li>
 *   <li><b>detail</b> — 面向开发者的详细错误信息（如具体哪个字段校验失败）。
 *   生产环境中可考虑通过开关控制是否返回此字段，避免敏感信息泄露。</li>
 *   <li><b>path</b> — 触发错误的请求 URI，便于日志分析和问题定位。</li>
 *   <li><b>timestamp</b> — 错误发生时间戳，用于日志关联和时序分析。</li>
 * </ul>
 *
 * <h2>与 Spring Boot 默认错误处理的区别</h2>
 * <p>Spring Boot 默认的 BasicErrorController 也提供错误响应，但格式固定且不便定制。
 * 使用自定义 ErrorResponse + @ControllerAdvice 全局异常处理器，可以获得完全的格式控制权。</p>
 *
 * @author Spring Demo Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "错误响应")
public class ErrorResponse {

    /**
     * HTTP 状态码。
     * <p>与 HTTP 响应头中的 status code 值一致，
     * 如 {@link org.springframework.http.HttpStatus#NOT_FOUND} 对应 404。
     * 将状态码放入 JSON body 中，方便客户端在无法读取 HTTP 头时仍能判断错误类型。</p>
     */
    @Schema(description = "错误代码")
    private int code;

    /**
     * 面向用户的错误消息。
     * <p>简短、无技术细节，可安全地直接展示在用户界面上。</p>
     */
    @Schema(description = "错误消息")
    private String message;

    /**
     * 详细的错误描述（面向开发者）。
     * <p>包含具体是哪个字段校验失败、什么原因等排查信息。
     * 注意：生产环境应谨慎控制此字段的内容，避免泄露内部架构信息。</p>
     */
    @Schema(description = "详细信息")
    private String detail;

    /**
     * 触发异常的请求路径。
     * <p>格式例如 "/api/users/abc123"。从 WebRequest 的 description 中提取，
     * 通过截取 "uri=" 前缀后得到纯净的路径字符串。</p>
     */
    @Schema(description = "请求路径")
    private String path;

    /**
     * 错误发生的时间戳。
     * <p>记录服务器端错误发生的精确时刻，便于在分布式系统中通过时间戳关联多个服务的日志。</p>
     */
    @Schema(description = "时间戳")
    private LocalDateTime timestamp;
}
