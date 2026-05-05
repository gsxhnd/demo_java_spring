package com.example.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户响应 DTO — 用于向客户端返回用户数据。
 *
 * <h2>DTO 模式的响应侧：为什么要用 UserResponse 而非直接返回 User 实体？</h2>
 * <p>响应 DTO 和请求 DTO 的职责不同，分开定义的原因：</p>
 * <ol>
 *   <li><b>暴露字段控制</b>：实体类 User 可能包含内部字段（如持久化状态、外键引用），
 *   响应 DTO 可以有选择性地暴露信息，避免向客户端泄露内部实现细节。</li>
 *   <li><b>字段组合灵活</b>：响应 DTO 可以组合多个实体/外部服务的数据，
 *   字段顺序和命名可以专门为前端消费优化（如嵌套对象、摘要信息等）。</li>
 *   <li><b>序列化配置独立</b>：可在 DTO 上使用 Jackson 注解（@JsonProperty、@JsonFormat 等）
 *   定制 JSON 输出格式，而不影响实体类的持久化行为。</li>
 *   <li><b>减少 API 版本耦合</b>：实体类字段变更不会直接破坏 API 响应结构。
 *   当数据库表结构变化时，只需调整服务层中 Entity → DTO 的转换逻辑。</li>
 *   <li><b>避免循环引用</b>：实体类可能存在双向关联（如 User ↔ Order），
 *   直接序列化实体可能导致 Jackson 的无限递归。DTO 是扁平的单向数据，不会出现此问题。</li>
 * </ol>
 *
 * <h2>LocalDateTime 与 JSON 序列化</h2>
 * <p>Java 8 引入的 {@link LocalDateTime} 表示不含时区的日期时间。
 * Spring Boot 自动配置的 Jackson 默认将其序列化为 ISO 8601 格式：
 * <code>"2026-05-05T13:53:57"</code>。可在 application.yml 中通过
 * <code>spring.jackson.date-format</code> 和 <code>spring.jackson.time-zone</code> 自定义格式。</p>
 *
 * <h2>@Builder 在此类中的作用</h2>
 * <p>主要在 {@link com.example.web.service.UserService#convertToResponse} 方法中使用建造者模式，
 * 将 User 实体对象逐个字段映射为 UserResponse，可读性优于使用 setter 逐个赋值：</p>
 * <pre>{@code
 * UserResponse.builder()
 *     .id(user.getId())
 *     .username(user.getUsername())
 *     ...
 *     .build();
 * }</pre>
 *
 * @author Spring Demo Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户响应")
public class UserResponse {

    /**
     * 用户唯一标识符（UUID v4）。
     * <p>由服务端在创建用户时自动生成（{@link java.util.UUID#randomUUID()}），
     * 客户端不可指定。使用随机 UUID 而非自增 ID 可避免 ID 被遍历猜测的安全风险。</p>
     */
    @Schema(description = "用户ID")
    private String id;

    /**
     * 用户名。
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 邮箱地址。
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 年龄。
     */
    @Schema(description = "年龄")
    private Integer age;

    /**
     * 账户状态。
     * <p>可能的值：ACTIVE（正常）、INACTIVE（停用）、DELETED（已删除）。
     * 在创建用户时由服务层自动设为 "ACTIVE"。</p>
     */
    @Schema(description = "状态")
    private String status;

    /**
     * 记录创建时间。
     * <p>由服务器在创建资源时自动设置，客户端不可修改。
     * 用于审计和排序。</p>
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 记录最后更新时间。
     * <p>每次更新用户信息时自动刷新为当前时间。
     * 用于乐观锁和变更追踪。</p>
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
