package com.example.observability.service;

import com.example.observability.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 启动信息服务 —— 演示 Spring 配置注入的两种方式，并通过日志输出启动信息。
 *
 * <h2>Spring 配置注入的两种方式（本项目同时使用）</h2>
 *
 * <h3>1. @ConfigurationProperties（类型安全绑定）</h3>
 * <ul>
 *   <li>使用场景：需要绑定一组相关配置（如 app.*）到一个 POJO 中</li>
 *   <li>优势：类型安全、支持 JSR-303 校验、IDE 自动补全、支持多层嵌套对象</li>
 *   <li>示例：{@link AppProperties} —— 通过 app.name / app.server.port 等绑定</li>
 * </ul>
 *
 * <h3>2. @Value（属性占位符）</h3>
 * <ul>
 *   <li>使用场景：需要注入单个属性值，且希望提供默认值</li>
 *   <li>优势：简洁，适合少量配置项</li>
 *   <li>示例：@Value("${server.port:8080}") —— 读取 server.port，未配置则默认 8080</li>
 *   <li>局限：不支持复杂类型（List/Map），不方便做校验</li>
 * </ul>
 *
 * <h2>@Service 注解</h2>
 * Spring 的定型注解（Stereotype Annotation），作用是：
 * <ul>
 *   <li>将该类标记为"服务层"组件</li>
 *   <li>自动被 @ComponentScan 检测并注册为 Spring Bean</li>
 *   <li>在语义上等价于 @Component，仅用于分层标识</li>
 * </ul>
 *
 * <h2>日志输出与可观测性</h2>
 * 由于 application.yml 中配置了 logging.structured.format.console: ecs，
 * 所有的 log.info 输出会以 <b>ECS（Elastic Common Schema）JSON 格式</b>输出到控制台。
 * ECS 是一种标准化的日志字段定义（如 @timestamp、log.level、message、trace.id、span.id 等），
 * 确保日志在 Elasticsearch 等平台中具有一致的结构，便于全文检索和聚合分析。
 */
@Slf4j
@Service
public class BootstrapService {

    /**
     * 类型安全绑定的应用配置（来自 application.yml 中 app.* 配置段）
     */
    private final AppProperties appProperties;

    /**
     * 使用 @Value 注入单个配置 —— Spring 应用名称。
     * ${spring.application.name:DefaultApp} 表示：
     * 读取 spring.application.name 配置，如果缺失则使用 "DefaultApp"。
     * 本项目在 application.yml 中配置为 spring-boot-observability-demo。
     */
    @Value("${spring.application.name:DefaultApp}")
    private String applicationName;

    /**
     * 使用 @Value 注入单个配置 —— 服务器端口。
     * ${server.port:8080} 表示默认 8080 端口。
     */
    @Value("${server.port:8080}")
    private int serverPort;

    /**
     * 构造器注入 AppProperties。
     * 注意：@Value 注入的字段是通过反射设置的（非构造器参数），
     * 因此构造器中无法使用 @Value 的值 —— 它们在构造器执行后才被注入。
     */
    public BootstrapService(AppProperties appProperties) {
        this.appProperties = appProperties;
        log.info("BootstrapService 初始化");
    }

    /**
     * 打印启动配置信息 —— 同时展示 @ConfigurationProperties 和 @Value 两种注入方式的读取结果。
     *
     * 在 ECS 结构化日志格式下，每行输出的 JSON 都会包含：
     * <ul>
     *   <li>@timestamp：日志时间戳</li>
     *   <li>log.level：日志级别（INFO）</li>
     *   <li>log.logger：输出日志的类名</li>
     *   <li>message：日志消息</li>
     *   <li>service.name：服务名称</li>
     *   <li>ecs.version：ECS 规范版本</li>
     * </ul>
     */
    public void printBootstrapInfo() {
        log.info("========== Spring Boot 启动信息 ==========");
        // ---- 来自 @ConfigurationProperties（AppProperties）的值 ----
        log.info("应用名称: {}", appProperties.getName());
        log.info("应用版本: {}", appProperties.getVersion());
        log.info("应用描述: {}", appProperties.getDescription());
        log.info("服务器主机: {}", appProperties.getServer().getHost());
        log.info("服务器端口: {}", appProperties.getServer().getPort());
        log.info("上下文路径: {}", appProperties.getServer().getContextPath());
        // ---- 来自 @Value 注入的值 ----
        log.info("========== @Value 注入的值 ==========");
        log.info("Spring 应用名称: {}", applicationName);
        log.info("服务器端口: {}", serverPort);
        log.info("=========================================");
    }
}
