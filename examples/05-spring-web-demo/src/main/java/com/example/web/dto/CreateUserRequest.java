package com.example.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建用户请求 DTO（Data Transfer Object —— 数据传输对象）。
 *
 * <h2>核心概念：DTO 模式 — 为什么不用实体类直接接收请求？</h2>
 * <p>DTO 是专门用于在不同层之间传输数据的简单对象，不包含业务逻辑。使用独立的请求 DTO 而非直接暴露实体类，原因如下：</p>
 * <ol>
 *   <li><b>安全性</b>：实体类（如 User）可能包含不应让客户端设置的字段（如 id、createdAt、status 等）。
 *   使用 DTO 可以精确控制客户端能提交哪些字段，防止"Mass Assignment"攻击——
 *   恶意用户无法通过额外字段篡改敏感属性。</li>
 *   <li><b>职责分离</b>：实体类对应数据库结构，请求 DTO 对应 API 契约。数据库变不会直接破坏 API，
 *   反之亦然。</li>
 *   <li><b>独立的校验规则</b>：请求 DTO 上可以定义创建场景特定的校验规则，而实体类专注于数据持久化。</li>
 *   <li><b>独立的 API 文档</b>：DTO 上的 @Schema 注解使 Swagger 文档更清晰（只展示客户端需要关心的字段）。</li>
 *   <li><b>版本控制</b>：当 API 需要升级时，可以创建新的 DTO（如 CreateUserV2Request），
 *   而不影响实体类和其他 API 版本。</li>
 * </ol>
 *
 * <h2>Lombok 注解说明</h2>
 * <ul>
 *   <li><b>@Data</b> — 组合注解，等价于 @Getter + @Setter + @RequiredArgsConstructor + @ToString + @EqualsAndHashCode。
 *   在编译时自动生成所有字段的 getter/setter、toString、equals 和 hashCode 方法。</li>
 *   <li><b>@NoArgsConstructor</b> — 生成无参构造器。对于 DTO 非常重要：
 *   框架（如 Jackson 反序列化）通常先通过无参构造器创建对象，再通过 setter 设置字段值。</li>
 *   <li><b>@AllArgsConstructor</b> — 生成包含所有字段的全参构造器。与 @Builder 配合使用时需要此注解。</li>
 *   <li><b>@Builder</b> — 生成建造者模式的 API，详见下文。</li>
 * </ul>
 *
 * <h2>核心概念：@Builder — 建造者模式</h2>
 * <p>@Builder 是 Lombok 提供的一个强大注解，它在编译时为类生成一个内部静态 Builder 类。
 * Builder 模式允许以<b>流式（链式调用）</b>的方式构建对象，具有以下优势：</p>
 * <ul>
 *   <li><b>可读性高</b>：通过方法名明确每个参数的含义，无需记住构造器参数顺序</li>
 *   <li><b>可选参数友好</b>：可按需设置部分字段，其余保持默认值</li>
 *   <li><b>不可变对象支持</b>：配合全参构造器，Build 后得到的是一个字段完整的对象</li>
 *   <li><b>线程安全</b>：构建过程是线程安全的（每次 build 返回新对象）</li>
 * </ul>
 *
 * <p>示例用法：</p>
 * <pre>{@code
 * CreateUserRequest request = CreateUserRequest.builder()
 *     .username("john_doe")
 *     .email("john@example.com")
 *     .age(30)
 *     .build();
 * }</pre>
 *
 * <h2>Jakarta Bean Validation 注解详解</h2>
 * <p>这些注解定义了字段的合法性约束，通过控制器方法参数前的 @Valid 注解触发校验。
 * 校验失败时会抛出 MethodArgumentNotValidException，由全局异常处理器统一返回 400 响应。</p>
 *
 * <table border="1">
 *   <tr><th>注解</th><th>作用</th></tr>
 *   <tr><td>@NotBlank</td><td>字符串不能为 null，且去除前后空格后长度必须大于 0（即不能全是空白字符）</td></tr>
 *   <tr><td>@Size(min, max)</td><td>限制字符串/集合的长度范围 [min, max]</td></tr>
 *   <tr><td>@Email</td><td>字符串必须是合法的 Email 格式（含 @ 符号和域名部分）</td></tr>
 *   <tr><td>@NotNull</td><td>包装类型不能为 null（用于 Integer 等对象类型）</td></tr>
 *   <tr><td>@Min(value)</td><td>数值必须 >= 指定值</td></tr>
 *   <tr><td>@Max(value)</td><td>数值必须 <= 指定值</td></tr>
 * </table>
 *
 * <p>注意 @NotBlank 与 @NotNull 的区别：</p>
 * <ul>
 *   <li>@NotNull：仅校验不为 null，空字符串 "" 可以通过</li>
 *   <li>@NotEmpty：不为 null 且长度 > 0（如字符串 "" 不通过）</li>
 *   <li>@NotBlank：不为 null，且去除首尾空白后长度 > 0（"   " 不通过，比 @NotEmpty 更严格）</li>
 * </ul>
 *
 * <h2>@Schema 注解 — OpenAPI/Swagger 文档元数据</h2>
 * <p>@Schema 为每个字段在 Swagger UI 中生成文档说明：</p>
 * <ul>
 *   <li><b>description</b>：字段的中文描述</li>
 *   <li><b>example</b>：示例值，在 Swagger UI 的 "Try it out" 功能中作为默认值展示</li>
 * </ul>
 *
 * @author Spring Demo Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "创建用户请求")
public class CreateUserRequest {

    /**
     * 用户名。
     * <p>约束：不能为空（@NotBlank），长度在 3-20 个字符之间（@Size）。
     * @NotBlank 确保用户名不是 null 且不全是空白字符。</p>
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Schema(description = "用户名", example = "john_doe")
    private String username;

    /**
     * 邮箱地址。
     * <p>约束：不能为空（@NotBlank），必须符合邮箱格式（@Email）。
     * @Email 校验依赖于 Jakarta Bean Validation 的内置正则，会检查是否包含 "@" 符号和有效域名。
     * 注意：@Email 不保证邮箱真实存在，仅校验格式合法性。</p>
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "john@example.com")
    private String email;

    /**
     * 年龄。
     * <p>约束：不能为空（@NotNull，因为 Integer 是包装类型可为 null），
     * 范围在 18-150 之间（@Min + @Max）。
     * 使用 Integer 而非 int 是因为：null 表示"未提供此值"，而 int 的默认值是 0，
     * 无法区分"用户传了 0"和"用户没传此字段"。</p>
     */
    @NotNull(message = "年龄不能为空")
    @Min(value = 18, message = "年龄必须大于等于18")
    @Max(value = 150, message = "年龄必须小于等于150")
    @Schema(description = "年龄", example = "30")
    private Integer age;
}
