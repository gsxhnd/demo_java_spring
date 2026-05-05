package com.example.boot.service;

import com.example.boot.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 启动信息服务 — 演示两种配置注入方式（@ConfigurationProperties vs @Value）的实际使用效果。
 *
 * <h2>@Service 注解</h2>
 * <p>
 * {@code @Service} 是 {@code @Component} 的特化形式（特殊的组件注解），
 * 用于标记服务层（Service Layer）的类。它继承自 @Component，功能和 @Component 完全一致，
 * 但通过语义化的名称表达了本类在分层架构中的角色：
 * </p>
 * <ul>
 *   <li><b>@Controller</b> → 表现层 / Web 层</li>
 *   <li><b>@Service</b> → 业务逻辑层</li>
 *   <li><b>@Repository</b> → 数据访问层</li>
 * </ul>
 * <p>
 * 在 Spring Boot 中，所有带有这些注解的类都会被 @ComponentScan 自动扫描并注册到 Spring 容器中。
 * </p>
 *
 * <h2>两种配置注入方式的在同一类中的对比</h2>
 * <p>
 * 本类同时使用了两种常见的 Spring 配置注入方式：
 * </p>
 * <ol>
 *   <li><b>@ConfigurationProperties（通过 AppProperties）</b>：
 *       通过构造函数注入 AppProperties Bean，获取应用自定义配置
 *       （app.name、app.version、app.server.* 等）。这是类型安全的批量绑定方式。</li>
 *   <li><b>@Value</b>：
 *       直接在字段上注入 Spring 内置配置项
 *       （spring.application.name、server.port）。适合零散的单个配置值。</li>
 * </ol>
 * <p>
 * <b>核心区别</b>：@ConfigurationProperties 将一组相关配置封装为强类型对象，
 * 支持 IDE 自动补全和编译期类型检查；@Value 则是运行时通过 SpEL 字符串表达式注入，
 * 拼写错误只能在运行时发现。
 * </p>
 *
 * @see AppProperties
 * @see Value
 */
@Slf4j
// @Service 将本类标记为 Spring 管理的服务层 Bean，@ComponentScan 会自动发现并注册它
@Service
public class BootstrapService {

    // 通过构造函数注入 AppProperties（类型安全的配置对象）
    // 注意这和使用 @Value 注入零散配置项的区别：
    // AppProperties 将 app.* 下的所有配置一次性、类型安全地绑定为一个对象
    private final AppProperties appProperties;

    /**
     * 通过 @Value 注入 {@code spring.application.name} 配置项。
     *
     * <p>
     * 语法：{@code @Value("${属性键:默认值}")}
     * </p>
     * <ul>
     *   <li>{@code spring.application.name} 是 Spring Boot 的内置配置项，
     *       定义在 application.yml 的 spring.application.name 中。</li>
     *   <li>{@code :DefaultApp} — 如果 YAML 中未配置此项，
     *       则使用默认值 "DefaultApp"。</li>
     *   <li>Spring 在创建 Bean 后、调用任何方法之前完成 @Value 的注入。</li>
     * </ul>
     */
    @Value("${spring.application.name:DefaultApp}")
    private String applicationName;

    /**
     * 通过 @Value 注入 {@code server.port} 配置项。
     *
     * <p>
     * Spring Boot 支持自动进行类型转换。虽然 YAML 中 {@code server.port: 8080}
     * 被解析为数字，但在 @Value 中它被自动转换为 int 类型并赋给 serverPort 字段。
     * 如果该字段是 String 类型，Spring 也会自动完成 int → String 的转换。
     * </p>
     */
    @Value("${server.port:8080}")
    private int serverPort;

    /**
     * 构造函数注入 AppProperties。
     *
     * <p>
     * Spring 在创建 BootstrapService Bean 时会先创建 AppProperties Bean，
     * 执行 @ConfigurationProperties 的绑定，然后将绑定好的对象传入此构造器。
     * 这里的 log.info 展示了构造函数的初始化时机（在 Bean 创建阶段）。
     * </p>
     *
     * @param appProperties 应用配置属性 Bean（Spring 自动传入）
     */
    public BootstrapService(AppProperties appProperties) {
        this.appProperties = appProperties;
        // 这行日志会在 Spring 容器启动过程（refresh 阶段）中打印，
        // 可用于理解 Spring Bean 的初始化顺序
        log.info("BootstrapService 初始化");
    }

    /**
     * 打印详细的启动信息到日志。
     *
     * <p>
     * 此方法演示了如何使用已注入的配置值，并展示了两种注入方式的值在同一业务方法中的集合使用：
     * </p>
     * <ul>
     *   <li>前半部分（应用名称/版本/描述/服务器信息）来自 {@code AppProperties}（@ConfigurationProperties 注入）</li>
     *   <li>后半部分（Spring 应用名称/服务器端口）来自 {@code @Value} 注入</li>
     * </ul>
     * <p>
     * 调用链：DemoController.startup() → BootstrapService.printBootstrapInfo()
     * 展示了标准的分层架构调用模式（Controller → Service）。
     * </p>
     */
    public void printBootstrapInfo() {
        // 打印通过 @ConfigurationProperties 绑定的应用自定义配置
        log.info("\n========== Spring Boot 启动信息 ==========");
        log.info("应用名称: {}", appProperties.getName());
        log.info("应用版本: {}", appProperties.getVersion());
        log.info("应用描述: {}", appProperties.getDescription());
        log.info("服务器主机: {}", appProperties.getServer().getHost());
        log.info("服务器端口: {}", appProperties.getServer().getPort());
        log.info("上下文路径: {}", appProperties.getServer().getContextPath());

        // 打印通过 @Value 注入的 Spring 内置配置，
        // 对比两种注入方式的输出结果
        log.info("\n========== @Value 注入的值 ==========");
        log.info("Spring 应用名称: {}", applicationName);
        log.info("服务器端口: {}", serverPort);
        log.info("=========================================\n");
    }
}
