package com.example.core.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类 — 演示 Spring Bean 的完整生命周期。
 *
 * <h3>@Service 注解详解：</h3>
 * <p>{@code @Service} 是 {@code @Component} 的特化注解，专门标注业务逻辑层的类。
 * 它在功能上与 {@code @Component} 没有区别，但语义上更清晰，同时有利于 Spring 自动扫描和 AOP 切入点表达式定位。</p>
 *
 * <h3>Spring Bean 的生命周期（5 个关键阶段）：</h3>
 * <table border="1">
 *   <tr><th>阶段</th><th>说明</th><th>本类中的体现</th></tr>
 *   <tr><td>1. 实例化</td><td>容器调用构造方法创建对象</td><td>构造方法 UserServiceImpl()</td></tr>
 *   <tr><td>2. 属性注入</td><td>通过构造器/setter 注入依赖</td><td>本类没有外部依赖</td></tr>
 *   <tr><td>3. 初始化</td><td>@PostConstruct 标注的方法自动执行</td><td>init() 方法</td></tr>
 *   <tr><td>4. 使用</td><td>Bean 就绪，处理业务请求</td><td>getUser() / saveUser()</td></tr>
 *   <tr><td>5. 销毁</td><td>容器关闭时 @PreDestroy 方法执行</td><td>destroy() 方法</td></tr>
 * </table>
 *
 * <h3>@PostConstruct 详解：</h3>
 * <ul>
 *   <li>在构造方法执行完毕、依赖注入完成后被容器自动调用。</li>
 *   <li>常用于：资源初始化（建立连接）、数据预热（加载缓存）、验证注入的依赖是否正确。</li>
 *   <li>只会被调用一次（因为是单例 Bean）。</li>
 * </ul>
 *
 * <h3>@PreDestroy 详解：</h3>
 * <ul>
 *   <li>在容器被销毁（如 {@code applicationContext.close()} 或 JVM 关闭钩子触发）时自动调用。</li>
 *   <li>常用于：释放连接池、关闭文件流、清理临时文件、发送离线通知。</li>
 *   <li>注意：如果 Bean 的 scope 是 prototype（原型），Spring 不会管理其销毁阶段。</li>
 * </ul>
 *
 * <h3>@PostConstruct 和 @PreDestroy 的替代方案：</h3>
 * <p>也可以通过实现 {@code InitializingBean} 的 {@code afterPropertiesSet()}
 * 和 {@code DisposableBean} 的 {@code destroy()} 接口来达到同样效果，
 * 但注解方式更简洁、不侵入业务代码，是 Spring 推荐的做法。</p>
 */
@Slf4j
@Service // 【核心注解】标记为业务层组件，Spring 容器自动创建并管理该 Bean
public class UserServiceImpl implements UserService {

    /**
     * 【生命周期第 1 步】构造方法 — Bean 实例化。
     * Spring 在扫描到 @Service 注解后，通过反射调用此构造方法创建 UserServiceImpl 实例。
     * 如果存在多个构造方法，需要配合 @Autowired 指定使用哪一个。
     */
    public UserServiceImpl() {
        log.info("1. 构造方法被调用");
    }

    /**
     * 【生命周期第 3 步】@PostConstruct 初始化回调。
     * 在构造方法和所有依赖注入完成后，Spring 容器自动调用此方法。
     * 此时该 Bean 已经是一个"完全可用"的对象，可以执行初始化逻辑。
     */
    @PostConstruct
    public void init() {
        log.info("3. @PostConstruct 初始化方法被调用");
    }

    /**
     * 【生命周期第 5 步】@PreDestroy 销毁回调。
     * 当 Spring 容器关闭时（调用 context.close() 或 JVM 正常退出），
     * 容器会回调此方法，让 Bean 有机会执行资源清理。
     */
    @PreDestroy
    public void destroy() {
        log.info("5. @PreDestroy 销毁方法被调用");
    }

    /**
     * 获取用户信息。
     * 模拟从业务层获取用户数据（实际项目可能涉及缓存、权限校验等）。
     *
     * @param userId 用户唯一标识
     * @return 格式化的用户信息字符串
     */
    @Override
    public String getUser(String userId) {
        log.info("获取用户: {}", userId);
        return "User{id='" + userId + "', name='John Doe'}";
    }

    /**
     * 保存用户数据。
     * 模拟业务层的持久化操作（实际项目可能涉及数据校验、事件发布等）。
     *
     * @param userId   用户唯一标识
     * @param userData 用户数据
     */
    @Override
    public void saveUser(String userId, String userData) {
        log.info("保存用户: {}, 数据: {}", userId, userData);
    }
}
