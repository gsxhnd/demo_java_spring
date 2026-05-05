package com.example.web.service;

import com.example.web.dto.CreateUserRequest;
import com.example.web.dto.UserResponse;
import com.example.web.entity.User;
import com.example.web.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 用户业务服务 — 包含核心业务逻辑，模拟数据库操作。
 *
 * <h2>核心概念：@Service 注解</h2>
 * <p>@Service 是 @Component 的特化版本（语义子类），其行为与 @Component 完全相同——
 * 将标注的类注册为 Spring IoC 容器中的 Bean。它是 Spring 的<b>构造型（Stereotype）</b>注解之一：</p>
 * <ul>
 *   <li>@Controller / @RestController — 表示 Web 层的控制器</li>
 *   <li>@Service — 表示业务逻辑层的服务</li>
 *   <li>@Repository — 表示数据访问层（DAO），额外提供持久层异常转换</li>
 *   <li>@Component — 通用的 Spring 组件，当不属以上分类时使用</li>
 * </ul>
 * <p>虽然 @Service 和 @Component 在行为上等价，但使用 @Service 有更好的语义表达：
 * 开发者看到 @Service 就立即知道这是业务逻辑层。</p>
 *
 * <h2>三层架构在本项目中的体现</h2>
 * <pre>
 * Controller 层（Web 层）    → UserController     — 接收 HTTP 请求，调用 Service
 * Service 层（业务逻辑层）  → UserService        — 业务逻辑，Entity ↔ DTO 转换
 * Repository 层（数据访问层）→ （本学习项目省略，用 ConcurrentHashMap 模拟）</pre>
 *
 * <h2>核心概念：ConcurrentHashMap — 线程安全的内存存储</h2>
 * <p>本学习项目不使用数据库，而是用 {@link ConcurrentHashMap} 作为内存中的"数据库"。</p>
 *
 * <h3>为什么用 ConcurrentHashMap 而非 HashMap？</h3>
 * <p>Spring Boot 内嵌的 Tomcat 默认使用多线程处理并发请求（线程池大小约 200）。
 * 如果多个线程同时读写一个普通的 {@link HashMap}，可能发生：</p>
 * <ul>
 *   <li><b>数据不一致</b>：一个线程 put 的同时另一个线程 get，可能读到中间状态</li>
 *   <li><b>ConcurrentModificationException</b>：遍历时其他线程修改了 Map 结构</li>
 *   <li><b>死循环（JDK 7）</b>：扩容时的链表环形引用导致 CPU 100%</li>
 * </ul>
 *
 * <h3>ConcurrentHashMap 如何解决线程安全问题？</h3>
 * <ul>
 *   <li><b>分段锁（JDK 7）</b> / <b>CAS + synchronized（JDK 8+）</b>：将数据分成多个段，
 *   不同段可以并行读写，只有操作同一段时才需要竞争锁</li>
 *   <li><b>读操作不加锁</b>：get() 操作不需要加锁（volatile 保证可见性），
 *   非常适合"读多写少"的缓存场景</li>
 *   <li><b>弱一致性迭代器</b>：遍历时不会抛出 ConcurrentModificationException，
 *   可以容忍遍历过程中其他线程对 Map 的修改</li>
 * </ul>
 *
 * <h3>生产环境中的替代方案</h3>
 * <ul>
 *   <li><b>Redis</b> — 分布式缓存，多实例共享数据，支持持久化和过期策略</li>
 *   <li><b>Caffeine / Guava Cache</b> — 本地高性能缓存，支持 LRU 淘汰和时间过期</li>
 *   <li><b>关系型数据库（MySQL/PostgreSQL）</b> — 配合 JPA/Hibernate 实现完整持久化</li>
 * </ul>
 *
 * <h2>DTO 转换模式</h2>
 * <p>本类中的 <code>convertToResponse()</code> 私有方法演示了实体 → DTO 的手动转换。
 * 在实际项目中，可以使用以下工具简化转换：</p>
 * <ul>
 *   <li><b>MapStruct</b> — 编译期生成转换代码，零运行时开销，最推荐</li>
 *   <li><b>ModelMapper</b> — 基于反射的自动映射，配置灵活</li>
 *   <li><b>手动映射</b> — 最直接、最可控、性能最好（当前项目的选择）</li>
 * </ul>
 *
 * @author Spring Demo Team
 */
@Slf4j
@Service
public class UserService {

    /**
     * 内存中的用户数据存储（模拟数据库）。
     *
     * <p>使用 {@link ConcurrentHashMap} 而非 {@link HashMap} 的原因：
     * Spring Boot 是多线程处理请求的，ConcurrentHashMap 保证多线程并发读写时不会出现
     * 数据不一致或 ConcurrentModificationException 的问题。</p>
     *
     * <p>Key: 用户 UUID（String）<br>Value: 用户实体对象（User）</p>
     */
    private final Map<String, User> userStore = new ConcurrentHashMap<>();

    /**
     * 获取所有用户列表。
     *
     * <h2>Stream API 转换流程</h2>
     * <ol>
     *   <li><code>userStore.values()</code> — 获取 Map 中所有 User 实体对象组成的 Collection</li>
     *   <li><code>.stream()</code> — 创建流（Stream），进入函数式编程模式</li>
     *   <li><code>.map(this::convertToResponse)</code> — 将每个 User 实体通过 convertToResponse 方法转换为 UserResponse DTO。
     *   方法引用 <code>this::convertToResponse</code> 等价于 <code>user -> convertToResponse(user)</code></li>
     *   <li><code>.collect(Collectors.toList())</code> — 将流中的结果收集到一个新的 List 中</li>
     * </ol>
     *
     * <h2>关于 Stream 的性能说明</h2>
     * <p>对于小数据量（本学习项目），Stream 和传统 for 循环性能差异可忽略。
     * Stream 的优势在于代码表达力——<code>map</code>、<code>filter</code>、
     * <code>collect</code> 等操作一目了然。</p>
     *
     * @return 所有用户的 DTO 列表（可能为空列表，但不会是 null）
     */
    public List<UserResponse> getAllUsers() {
        log.info("获取所有用户");
        return userStore.values().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据用户 ID 获取单个用户。
     *
     * <h2>ConcurrentHashMap.get() — 线程安全的读取</h2>
     * <p>ConcurrentHashMap 的 get() 操作不需要加锁（通过 volatile 保证字段可见性），
     * 因此即使在多线程环境下读取，性能也几乎等同于 HashMap。</p>
     *
     * <h2>异常驱动流程控制</h2>
     * <p>当用户不存在时，不返回 null 或 Optional.empty()，而是直接抛出异常：</p>
     * <pre>{@code
     * if (user == null) {
     *     throw new UserNotFoundException("用户不存在: " + id);
     * }
     * }</pre>
     * <p>这种模式被称为"异常驱动的流程控制"，优点：</p>
     * <ul>
     *   <li>调用方（Controller）无需对 null 做防御性检查</li>
     *   <li>全局异常处理器统一将异常转换为 404 JSON 响应</li>
     *   <li>避免了 null 传播导致的 NullPointerException</li>
     * </ul>
     *
     * @param id 用户唯一标识符
     * @return 用户 DTO
     * @throws UserNotFoundException 如果指定 ID 的用户不存在
     */
    public UserResponse getUserById(String id) {
        log.info("获取用户: {}", id);
        User user = userStore.get(id);
        if (user == null) {
            throw new UserNotFoundException("用户不存在: " + id);
        }
        return convertToResponse(user);
    }

    /**
     * 创建新用户。
     *
     * <h2>UUID 生成策略</h2>
     * <p>使用 {@link UUID#randomUUID()} 生成全局唯一的用户 ID。UUID v4 基于随机数，
     * 碰撞概率极低（约 1/2^122），适合分布式系统。但也存在缺点：
     * 无序性导致数据库 B+ 树索引性能下降（实际项目中通常使用雪花算法或自增 ID）。</p>
     *
     * <h2>@Builder 构造实体对象</h2>
     * <p>使用建造者模式创建 User 对象，字段可读性和顺序无关性优于直接使用构造器：</p>
     * <pre>{@code
     * User user = User.builder()
     *         .id(userId)
     *         .username(request.getUsername())
     *         ...
     *         .build();
     * }</pre>
     *
     * <h2>ConcurrentHashMap.put() — 线程安全的写入</h2>
     * <p>put() 操作在 ConcurrentHashMap 中通过 CAS（Compare-And-Swap）+ synchronized
     * 保证线程安全。多个线程同时创建不同用户时（不同的 key），可以并发执行互不阻塞。</p>
     *
     * @param request 包含新用户信息的请求 DTO（已经过 @Valid 校验）
     * @return 新创建的用户 DTO，包含服务端生成的 ID、时间戳和默认状态
     */
    public UserResponse createUser(CreateUserRequest request) {
        // 生成全局唯一的用户 ID（UUID v4）
        String userId = UUID.randomUUID().toString();
        // 获取当前时间，创建时间和更新时间设为同一时刻
        LocalDateTime now = LocalDateTime.now();

        /*
         * 使用 @Builder 模式创建 User 实体对象。
         * 注意：id、status、createdAt、updatedAt 由服务端控制，不从 request 中获取。
         * 这就是 DTO 模式的核心理念——客户端只能设置允许的字段。
         */
        User user = User.builder()
                .id(userId)
                .username(request.getUsername())
                .email(request.getEmail())
                .age(request.getAge())
                .status("ACTIVE")        // 新用户默认为正常状态
                .createdAt(now)           // 创建时间由服务端设置
                .updatedAt(now)           // 初始时更新时间与创建时间相同
                .build();

        // 将新建用户放入内存存储（模拟 INSERT INTO users ...）
        userStore.put(userId, user);
        log.info("创建用户: {}", userId);

        // 将实体转换为 DTO 后返回（不直接暴露实体）
        return convertToResponse(user);
    }

    /**
     * 更新已有用户。
     *
     * <h2>先查后改模式</h2>
     * <p>更新操作的标准流程：</p>
     * <ol>
     *   <li>通过 ID 查询用户是否存在（不存在则抛出异常）</li>
     *   <li>更新允许修改的字段（username、email、age）</li>
     *   <li>刷新 updatedAt 时间戳</li>
     *   <li>将更新后的对象放回存储（ConcurrentHashMap.put 会覆盖旧值）</li>
     * </ol>
     *
     * <h2>注意：本实现是"全量更新"而非"部分更新"</h2>
     * <p>PUT 方法接收完整的 CreateUserRequest（所有字段），因此会覆盖所有可修改字段。
     * 如果需要部分更新（如只改邮箱不改用户名），应使用 PATCH 方法并设计专门的 PartialUpdateRequest DTO。
     * 这是 REST 中 PUT（全量替换）与 PATCH（部分更新）的核心语义差异。</p>
     *
     * @param id      要更新的用户 ID
     * @param request 包含更新后数据（全部字段）的请求 DTO
     * @return 更新后的用户 DTO
     * @throws UserNotFoundException 如果指定 ID 的用户不存在
     */
    public UserResponse updateUser(String id, CreateUserRequest request) {
        User user = userStore.get(id);
        if (user == null) {
            throw new UserNotFoundException("用户不存在: " + id);
        }

        /*
         * 更新用户的可变字段。
         * 注意：id 和 createdAt 不会被更新（保持不变），
         * 这体现了 DTO 的保护作用——客户端无法通过请求体篡改这些字段。
         */
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setAge(request.getAge());
        user.setUpdatedAt(LocalDateTime.now());  // 刷新更新时间戳

        // ConcurrentHashMap.put() 会覆盖已有的值
        userStore.put(id, user);
        log.info("更新用户: {}", id);

        return convertToResponse(user);
    }

    /**
     * 删除用户。
     *
     * <h2>ConcurrentHashMap.remove() — 线程安全的删除</h2>
     * <p>remove(key) 在 ConcurrentHashMap 中同样是线程安全的。
     * 如果多个线程同时删除同一个 key，只有一个线程会成功删除，
     * 其他线程调用 get() 会返回 null（然后发现不存在）。</p>
     *
     * <h2>物理删除 vs 逻辑删除</h2>
     * <p>本方法执行的是<b>物理删除</b>（数据从 Map 中彻底移除）。
     * 生产环境通常使用<b>逻辑删除</b>（将 status 设为 "DELETED"，数据仍保留）：</p>
     * <ul>
     *   <li>逻辑删除的优点：可恢复、审计记录完整、外键关系不丢失</li>
     *   <li>逻辑删除的缺点：占用存储空间、查询需要过滤已删除记录</li>
     * </ul>
     *
     * @param id 要删除的用户 ID
     * @throws UserNotFoundException 如果指定 ID 的用户不存在
     */
    public void deleteUser(String id) {
        User user = userStore.get(id);
        if (user == null) {
            throw new UserNotFoundException("用户不存在: " + id);
        }

        // 从 Map 中移除（物理删除）
        userStore.remove(id);
        log.info("删除用户: {}", id);
    }

    /**
     * 将 User 实体对象转换为 UserResponse DTO（私有工具方法）。
     *
     * <h2>DTO 转换的设计考量</h2>
     * <p>此方法封装了 Entity → DTO 的映射逻辑，所有服务层方法都通过它来准备返回数据。
     * 集中转换逻辑的意义：</p>
     * <ul>
     *   <li><b>DRY 原则</b>（Don't Repeat Yourself）：映射逻辑只写一次，多处复用</li>
     *   <li><b>单一修改点</b>：当 User 实体添加新字段时，只需在此方法中决定是否暴露到 DTO</li>
     *   <li><b>安全边界</b>：此方法是实体离开服务层的最后一道关卡，可以在此处过滤敏感字段</li>
     * </ul>
     *
     * <h2>@Builder 建造者模式的实践</h2>
     * <p>使用 UserResponse.builder() 链式设置每个字段的值，最后调用 .build() 创建对象。
     * 与传统的 setter 方式相比，builder 模式明确表达了"我在构建一个新的 UserResponse 对象"的意图，
     * 且所有字段在一组连续的调用中设置，减少了中间状态暴露的可能性。</p>
     *
     * @param user 要转换的用户实体
     * @return 对应的用户响应 DTO（只包含安全可暴露的字段）
     */
    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .age(user.getAge())
                .status(user.getStatus())          // 暴露给前端的状态信息
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
