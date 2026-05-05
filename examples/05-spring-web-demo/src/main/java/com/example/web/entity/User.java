package com.example.web.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类 — 代表系统中的"用户"这一业务概念。
 *
 * <h2>核心概念：实体类（Entity）与 DTO 的区别</h2>
 * <p>在典型的 Spring 分层架构中，不同类型的类承担不同职责：</p>
 * <table border="1">
 *   <tr><th>类型</th><th>位置</th><th>职责</th><th>生命周期</th></tr>
 *   <tr>
 *     <td>Entity（实体类）</td>
 *     <td>entity/ 包</td>
 *     <td>映射数据库表结构，代表持久化的业务数据</td>
 *     <td>与数据库记录一一对应</td>
 *   </tr>
 *   <tr>
 *     <td>DTO（数据传输对象）</td>
 *     <td>dto/ 包</td>
 *     <td>在 Controller ↔ Service ↔ Client 之间传输数据</td>
 *     <td>随请求/响应创建和销毁</td>
 *   </tr>
 *   <tr>
 *     <td>VO（视图对象）</td>
 *     <td>（可选）</td>
 *     <td>专门为前端页面定制数据结构（如聚合多个实体的字段）</td>
 *     <td>随视图渲染创建和销毁</td>
 *   </tr>
 * </table>
 *
 * <h2>本项目的简化设计</h2>
 * <p>这是一个学习项目，因此使用 {@link java.util.concurrent.ConcurrentHashMap} 作为内存存储替代数据库。
 * 在实际项目中，此实体类会添加 JPA 注解（@Entity, @Id, @Column 等）来映射到数据库表。</p>
 *
 * <h2>为何不直接暴露实体类给前端？</h2>
 * <ol>
 *   <li><b>安全风险</b>：实体类可能包含敏感字段（密码哈希、内部状态标记），直接序列化到 JSON 会泄露数据</li>
 *   <li><b>耦合风险</b>：数据库表结构变化（如添加字段、改名）会直接破坏 API，导致所有客户端必须同步更新</li>
 *   <li><b>序列化问题</b>：JPA 实体可能包含懒加载代理、双向关联等，JSON 序列化时可能导致 LazyInitializationException 或无限递归</li>
 *   <li><b>字段膨胀</b>：实体类按数据库范式设计，字段可能很多；而 API 只需要返回部分字段，直接暴露会浪费带宽</li>
 * </ol>
 *
 * @author Spring Demo Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * 用户唯一标识符。
     * <p>在实际项目中通常映射到数据库主键。这里使用 UUID 字符串，
     * 在创建用户时由 {@link com.example.web.service.UserService} 自动生成。</p>
     */
    private String id;

    /**
     * 用户名 — 用户的显示名称。
     * <p>在请求 DTO {@link com.example.web.dto.CreateUserRequest} 中，
     * 此字段受 @NotBlank 和 @Size(min=3, max=20) 约束。</p>
     */
    private String username;

    /**
     * 邮箱地址。
     */
    private String email;

    /**
     * 年龄。
     * <p>使用 Integer（包装类型）而非 int（基本类型）。在 JPA 场景下，
     * 包装类型可映射为可空数据库列（允许 NULL），基本类型则强制 NOT NULL。</p>
     */
    private Integer age;

    /**
     * 账户状态 — 内部控制字段，不暴露给请求方。
     * <p>可能的值：</p>
     * <ul>
     *   <li>ACTIVE — 正常状态，可以正常使用系统</li>
     *   <li>INACTIVE — 已停用，管理员可重新激活</li>
     *   <li>DELETED — 逻辑删除（软删除），数据保留但用户不可见</li>
     * </ul>
     * <p>注意：客户端不能直接设置此字段，它在创建时由服务层自动设为 "ACTIVE"。
     * 这是使用 DTO 而非直接暴露实体的一个典型例子。</p>
     */
    private String status; // ACTIVE, INACTIVE, DELETED

    /**
     * 记录创建时间。
     * <p>由服务端在创建对象时自动设置为当前时间（{@link LocalDateTime#now()}），
     * 客户端无法指定。用于审计追踪和数据排序。</p>
     */
    private LocalDateTime createdAt;

    /**
     * 记录最后更新时间。
     * <p>每次通过 {@link com.example.web.service.UserService#updateUser} 更新用户时
     * 自动刷新为当前时间。实现简单的变更时间追踪。</p>
     */
    private LocalDateTime updatedAt;
}
