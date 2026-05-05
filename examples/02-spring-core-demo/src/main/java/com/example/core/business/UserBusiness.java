package com.example.core.business;

import com.example.core.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户业务类 — 演示 Spring 的"构造器注入"（Constructor Injection）模式。
 *
 * <h3>@Service vs @Component 本质：</h3>
 * <p>@Service 和 @Component 在 Spring 底层机制中完全相同，@Service 只是 @Component
 * 的一个"语义标记（stereotype）"。使用 @Service 的好处是：</p>
 * <ul>
 *   <li>代码意图清晰，看到 @Service 就知道这是业务层 Bean。</li>
 *   <li>便于工具扫描和分层检查（例如 Checkstyle / ArchUnit 可以约束层间依赖）。</li>
 *   <li>AOP 切入点可以使用 {@code within(@org.springframework.stereotype.Service *)} 匹配所有业务层。</li>
 * </ul>
 *
 * <h3>构造器注入详解（Constructor Injection）：</h3>
 *
 * <p><b>什么是构造器注入？</b></p>
 * <p>通过构造方法的参数来接收依赖对象，Spring 在创建 Bean 时自动从容器中获取依赖并传入。
 * 这是 Spring 官方推荐的三种注入方式之首（另两种是 Setter 注入和字段注入）。</p>
 *
 * <p><b>为什么字段 field 声明为 private final？</b></p>
 * <ol>
 *   <li><b>private</b>：封装性，外部不可直接访问，只能通过类的公共方法来间接操作。</li>
 *   <li><b>final</b>：不可变性，一旦在构造方法中赋值后就不能被修改，确保依赖不会被意外替换，
 *       也保证线程安全（不可变对象天然线程安全）。</li>
 *   <li>结合 final + 构造器参数，Spring 会保证依赖在对象创建时就完全就绪，
 *       不会出现 NPE（NullPointerException）问题。</li>
 * </ol>
 *
 * <p><b>Spring 如何自动检测构造器？</b></p>
 * <p>如果类只有一个构造方法，Spring 会直接使用它并自动注入所有参数，
 * 不需要额外标注 {@code @Autowired}（从 Spring 4.3 开始）；如果有多个构造方法，
 * 则需要用 @Autowired 标记 Spring 应该使用哪一个。</p>
 *
 * <h3>三种注入方式对比：</h3>
 * <table border="1">
 *   <tr><th>方式</th><th>代码</th><th>优点</th><th>缺点</th></tr>
 *   <tr>
 *     <td>构造器注入</td>
 *     <td>{@code public X(Y y) \{ this.y = y; }}</td>
 *     <td>依赖不可变；编译期保证依赖不为 null；便于测试</td>
 *     <td>参数多时构造方法冗长（可考虑 Lombok @RequiredArgsConstructor）</td>
 *   </tr>
 *   <tr>
 *     <td>Setter 注入</td>
 *     <td>{@code @Autowired public void setY(Y y)}}</td>
 *     <td>可选依赖可以用 setter；可在运行时更换依赖</td>
 *     <td>依赖可变；可能出现 NPE；不适用于必须依赖</td>
 *   </tr>
 *   <tr>
 *     <td>字段注入</td>
 *     <td>{@code @Autowired private Y y;}</td>
 *     <td>代码最简短</td>
 *     <td>无法声明为 final；难以测试（需要反射注入）；隐藏了依赖关系</td>
 *   </tr>
 * </table>
 */
@Slf4j
@Service // 【核心注解】标记此类为业务逻辑层 Bean，Spring 自动将其纳入容器管理
public class UserBusiness {

    // private final：确保依赖不可变，且构造注入后一定不为 null
    private final UserRepository userRepository;

    /**
     * 构造器注入 — Spring 会自动从 IoC 容器中找到唯一的 UserRepository 实现类（UserRepositoryImpl）
     * 并注入到这里。因为该类只有一个构造方法，所以无需显式标注 @Autowired。
     *
     * @param userRepository 由 Spring 容器自动注入的 UserRepository Bean
     */
    public UserBusiness(UserRepository userRepository) {
        this.userRepository = userRepository; // 将注入的依赖赋值给 final 字段
        log.info("UserBusiness 通过构造器注入了 UserRepository");
    }

    /**
     * 获取用户信息。
     * 调用 Repository 层查询数据并返回，演示分层架构中"业务层调用数据层"的模式。
     *
     * @param userId 用户唯一标识
     * @return 从 Repository 查询到的用户信息对象
     */
    public Object getUserInfo(String userId) {
        log.info("业务逻辑：获取用户信息");
        return userRepository.findById(userId); // 将请求委托给数据访问层
    }

    /**
     * 创建用户。
     * 调用 Repository 层保存数据，演示业务层如何编排持久化操作。
     *
     * @param userId   用户唯一标识
     * @param userData 用户数据
     */
    public void createUser(String userId, String userData) {
        log.info("业务逻辑：创建用户");
        userRepository.save(userId, userData); // 将保存请求委托给数据访问层
    }
}
