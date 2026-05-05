package com.example.observability;

import com.example.observability.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * 可观测性（Observability）演示应用的启动入口。
 *
 * <h2>什么是可观测性？</h2>
 * 可观测性（Observability）是现代分布式系统中用于"通过外部输出推断系统内部状态"的能力。
 * 它由三大支柱组成：
 * <ul>
 *   <li><b>Logs（日志）</b>：记录离散事件，如请求日志、错误日志。本项目通过 ECS 结构化日志输出。</li>
 *   <li><b>Metrics（指标）</b>：聚合的时间序列数据，如请求计数、响应时间、CPU 使用率。通过 Micrometer + Actuator 暴露。</li>
 *   <li><b>Traces（链路追踪）</b>：跨服务的一次完整请求调用链。通过 OpenTelemetry + Micrometer Observation 实现。</li>
 * </ul>
 *
 * <h2>本项目用到的核心技术栈</h2>
 * <ul>
 *   <li><b>Spring Boot Actuator</b>：暴露 /actuator/health（健康检查）、/actuator/metrics（指标查询）、
 *       /actuator/info（应用信息）、/actuator/env（环境变量）、/actuator/loggers（日志级别动态调整）等端点。</li>
 *   <li><b>Micrometer</b>：指标门面（Metrics Facade），提供统一的指标 API（Counter、Timer、Gauge 等），
 *       支持对接 Prometheus、Datadog、InfluxDB 等多种监控后端。</li>
 *   <li><b>OpenTelemetry（OTel）</b>：云原生计算基金会（CNCF）的可观测性标准，统一 Trace、Metric、Log 的数据采集与导出格式。
 *       通过 spring-boot-starter-opentelemetry 自动配置。</li>
 *   <li><b>结构化日志（Structured Logging）</b>：使用 ECS（Elastic Common Schema）格式输出 JSON 日志，
 *       便于 Elasticsearch / Logstash / Kibana（ELK）等日志平台解析和检索。</li>
 * </ul>
 *
 * @see <a href="https://docs.spring.io/spring-boot/reference/actuator/">Spring Boot Actuator 官方文档</a>
 * @see <a href="https://micrometer.io/">Micrometer 官网</a>
 * @see <a href="https://opentelemetry.io/">OpenTelemetry 官网</a>
 */
@Slf4j // Lombok 注解：自动生成 private static final Logger log = LoggerFactory.getLogger(ObservabilityDemoApplication.class);
@SpringBootApplication // 组合注解：相当于 @Configuration + @EnableAutoConfiguration + @ComponentScan
public class ObservabilityDemoApplication {

    /**
     * 通过构造器注入 AppProperties。
     * Spring Boot 自动将 application.yml 中 app.* 配置绑定到这个对象中。
     * 使用构造器注入（而非 @Autowired 字段注入）的好处：
     * <ul>
     *   <li>依赖不可变（private final）</li>
     *   <li>方便单元测试（可以手动传入 mock 对象）</li>
     *   <li>避免循环依赖问题</li>
     * </ul>
     */
    private final AppProperties appProperties;

    public ObservabilityDemoApplication(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * Spring Boot 应用入口。
     * {@link SpringApplication#run(Class, String...)} 会执行以下步骤：
     * <ol>
     *   <li>创建 ApplicationContext（Spring IoC 容器）</li>
     *   <li>触发自动配置（AutoConfiguration），根据 classpath 中的依赖自动装配 Bean</li>
     *   <li>启动内嵌 Web 服务器（默认 Tomcat），监听 8080 端口</li>
     *   <li>发布 ApplicationReadyEvent（应用启动完成事件）</li>
     * </ol>
     */
    public static void main(String[] args) {
        SpringApplication.run(ObservabilityDemoApplication.class, args);
    }

    /**
     * 监听 {@link ApplicationReadyEvent} 事件 —— 该事件在 Spring 容器完全初始化、Web 服务器已启动后发布。
     *
     * <h2>Spring 启动事件顺序</h2>
     * <pre>
     * ApplicationStartingEvent  → 应用刚开始启动，Environment 未准备好
     * ApplicationEnvironmentPreparedEvent → Environment 已准备好，但上下文未创建
     * ApplicationContextInitializedEvent → ApplicationContext 已创建，但 Bean 未加载
     * ApplicationPreparedEvent → Bean 定义已加载，但实例化前
     * ApplicationStartedEvent → Bean 已实例化，命令行运行器调用前
     * <b>ApplicationReadyEvent → 应用完全就绪，可以接收请求</b>  ← 我们监听的
     * ApplicationFailedEvent → 启动失败时发布
     * </pre>
     *
     * <h2>在这里做什么？</h2>
     * 打印启动完成横幅，告知用户可以通过哪些 URL 访问 Actuator 端点。
     * 由于启用了 ECS 结构化日志（application.yml 中配置了 logging.structured.format.console: ecs），
     * 这些 log.info 输出会自动以 JSON 格式渲染到控制台，无需手动拼 JSON。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("========================================");
        log.info("   {} 启动完成", appProperties.getName());
        log.info("   版本: {}", appProperties.getVersion());
        /*
         * Actuator Health 端点：返回应用健康状态（UP/DOWN），可扩展自定义健康指标。
         * 配置了 management.endpoint.health.show-details: always，会显示详细组件状态。
         */
        log.info("   Actuator Health: http://localhost:{}/actuator/health", appProperties.getServer().getPort());
        /*
         * Actuator Metrics 端点：返回所有已注册的指标（JVM 指标 + 自定义指标），
         * 通过 management.endpoints.web.exposure.include 控制哪些端点对外暴露。
         * 可选值：health, metrics, info, env, loggers, mappings, threaddump, heapdump, prometheus 等。
         */
        log.info("   Actuator Metrics: http://localhost:{}/actuator/metrics", appProperties.getServer().getPort());
        log.info("========================================");
    }
}
