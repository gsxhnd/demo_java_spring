package com.example.observability.controller;

import com.example.observability.config.AppProperties;
import com.example.observability.service.BootstrapService;
import com.example.observability.service.MetricsService;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST API 控制器 —— 可观测性演示的 HTTP 入口。
 *
 * <h2>@RestController 的作用</h2>
 * 相当于 @Controller + @ResponseBody 的组合注解。每个方法的返回值会被自动序列化为 JSON 写入 HTTP Response Body。
 * 本项目使用 Jackson 作为默认 JSON 序列化器。
 *
 * <h2>@Observed 注解详解</h2>
 * 来自 Micrometer Observation 模块（micrometer-observation-api），用于声明式地创建一次"观察（Observation）"。
 * 在 OpenTelemetry 语境下，每一次 Observation 会对应生成一个 Span（操作跨度），构成 Trace（调用链）的一个节点。
 *
 * <h3>参数说明</h3>
 * <ul>
 *   <li><b>name</b>：Span 的名称（技术标识），通常使用点号分隔的小写命名，如 "api.hello"。</li>
 *   <li><b>contextualName</b>：Span 的上下文名称（人类可读），在 Jaeger/Zipkin 等 UI 中显示。</li>
 * </ul>
 *
 * <h3>底层原理</h3>
 * Spring AOP（面向切面编程）在方法调用前后植入逻辑：
 * <ol>
 *   <li>方法进入前：创建 Observation 并启动，生成一个新的 Span（如果已有 Trace Context 则作为子 Span）</li>
 *   <li>方法执行期间：通过 Observation.Event 或 lowCardinalityKeyValue 记录关键信息</li>
 *   <li>方法返回后：停止 Observation，Span 完成并导出到 OTel Exporter（本项目中为 logging exporter）</li>
 *   <li>方法抛异常后：记录异常信息到 Span，标记为错误状态</li>
 * </ol>
 *
 * <h2>@Value 注解</h2>
 * 从 Spring 的 Environment（包含 application.yml、环境变量、命令行参数等）中读取配置值。
 * 语法 "${property:defaultValue}"，冒号后为可选默认值。
 *
 * <h2>构造器注入（Constructor Injection）</h2>
 * 三个依赖均以 private final 声明 + 构造器注入。Spring 4.3+ 支持隐式自动装配：
 * 当类只有一个构造器时，即使不写 @Autowired，Spring 也会自动注入参数。
 */
@Slf4j
@RestController
@RequestMapping("/api") // 所有接口路径的统一前缀
public class DemoController {

    /**
     * 自定义应用配置（通过 @ConfigurationProperties 绑定自 application.yml）
     */
    private final AppProperties appProperties;

    /**
     * 启动信息服务 —— 用于打印启动配置信息
     */
    private final BootstrapService bootstrapService;

    /**
     * 指标服务 —— 演示 Micrometer Counter / Timer / Gauge 等指标的使用
     */
    private final MetricsService metricsService;

    /**
     * 从 spring 配置环境中读取 server.servlet.context-path 的值。
     * 如果未配置则使用空字符串 "" 作为默认值。
     *
     * @Value 是 SpEL（Spring Expression Language）的一部分，语法 "#{...}" 用于表达式计算，
     *        "${...}" 用于属性占位符替换。
     */
    @Value("${server.servlet.context-path:}")
    private String contextPath;

    public DemoController(AppProperties appProperties,
                          BootstrapService bootstrapService,
                          MetricsService metricsService) {
        this.appProperties = appProperties;
        this.bootstrapService = bootstrapService;
        this.metricsService = metricsService;
    }

    /**
     * Hello 接口 —— 演示 @Observed 声明式 Span 创建。
     *
     * <h2>可观测性效果</h2>
     * 当请求到达时：
     * <ol>
     *   <li>Spring WebFlux/WebMvc 自动创建一个 HTTP Server Span（如 "GET /api/hello"）</li>
     *   <li>@Observed 创建一个业务级别的子 Span "api.hello"，挂载到父 Span 下</li>
     *   <li>MetricsService#recordHelloRequest() 内部又创建了一个更低层级的子 Span "metrics.record-hello"</li>
     *   <li>三个 Span 形成一条完整的 Trace 链，在控制台中可通过 traceId 关联</li>
     * </ol>
     *
     * <h2>TraceId 与 SpanId</h2>
     * 每个 Trace 有一个全局唯一的 traceId，每个 Span 有自己的 spanId。
     * 所有日志输出（通过 ECS 结构化格式）都会自动携带 traceId 和 spanId，
     * 使得"日志、指标、链路追踪"三大支柱可以相互关联（Correlation）。
     *
     * @return 包含欢迎消息、应用名和版本号的 Map，Jackson 序列化为 JSON 返回
     */
    @GetMapping("/hello")
    @Observed(name = "api.hello", contextualName = "GET /api/hello")
    public Map<String, Object> hello() {
        log.info("收到 /api/hello 请求");
        // 递增 hello 请求计数器（Micrometer Counter），每次请求 +1
        metricsService.recordHelloRequest();
        return Map.of(
            "message", "欢迎来到 Spring Boot 可观测性演示",
            "appName", appProperties.getName(),
            "version", appProperties.getVersion()
        );
    }

    /**
     * 应用信息接口 —— 展示 @ConfigurationProperties 绑定效果。
     * 返回完整的应用配置信息，包括嵌套的 server 配置。
     * 此接口未标注 @Observed，但 Spring Web 仍会自动创建 HTTP 级别的 Span。
     */
    @GetMapping("/info")
    public Map<String, Object> getAppInfo() {
        log.info("收到 /api/info 请求");
        return Map.of(
            "name", appProperties.getName(),
            "version", appProperties.getVersion(),
            "description", appProperties.getDescription(),
            "server", Map.of(
                "host", appProperties.getServer().getHost(),
                "port", appProperties.getServer().getPort(),
                "contextPath", appProperties.getServer().getContextPath()
            )
        );
    }

    /**
     * 启动信息打印接口 —— 触发 BootstrapService 在日志中输出配置信息。
     * 用于验证 @Value 和 @ConfigurationProperties 两种配置注入方式是否都成功加载。
     */
    @GetMapping("/startup")
    public Map<String, String> startup() {
        bootstrapService.printBootstrapInfo();
        return Map.of("status", "启动信息已打印到日志");
    }

    /**
     * 慢请求模拟接口 —— 演示 @Observed + 耗时操作的 Span 标记。
     *
     * <h2>可观测性效果</h2>
     * <ul>
     *   <li>Thread.sleep(delayMs) 的耗时会被记录到 Span 的 duration 字段</li>
     *   <li>在 Jaeger/Zipkin UI 中可以直观看到这个 Span 的耗时（Latency）</li>
     *   <li>MetricsService 中的 requestTimer 记录了 1 秒的 Duration（用于演示 Timer 功能）</li>
     * </ul>
     *
     * @param delayMs 模拟的延迟时间（毫秒），默认 100ms
     * @return 包含实际耗时的 JSON 响应
     */
    @GetMapping("/slow")
    @Observed(name = "api.slow", contextualName = "GET /api/slow")
    public Map<String, Object> slowOperation(@RequestParam(defaultValue = "100") int delayMs)
            throws InterruptedException {
        log.info("收到慢请求，延迟 {}ms", delayMs);
        long start = System.currentTimeMillis();
        metricsService.recordSlowRequest();
        Thread.sleep(delayMs); // 模拟业务耗时操作，阻塞当前线程
        long duration = System.currentTimeMillis() - start;
        log.info("慢请求完成，实际耗时 {}ms", duration);
        return Map.of("message", "慢请求完成", "requestedDelayMs", delayMs, "actualDurationMs", duration);
    }

    /**
     * 错误模拟接口 —— 演示异常如何被记录到 Span 中。
     *
     * <h2>可观测性效果</h2>
     * 当 fail=true 时：
     * <ol>
     *   <li>抛出 RuntimeException</li>
     *   <li>Spring 自动将异常信息（异常类型、堆栈、消息）记录到当前 Span</li>
     *   <li>Span 的状态被标记为 ERROR</li>
     *   <li>在日志中（ECS 格式）会记录 error.stack_trace 字段</li>
     *   <li>Micrometer 的 errorRequestCounter 计数器 +1</li>
     * </ol>
     *
     * @param fail 是否触发业务异常，默认 false（正常返回）
     * @return 正常时返回成功消息，异常时返回 5xx 错误
     */
    @GetMapping("/error-simulate")
    public Map<String, String> simulateError(@RequestParam(defaultValue = "false") boolean fail) {
        log.info("收到模拟错误请求，fail={}", fail);
        metricsService.recordErrorRequest(); // 无论是否异常都计数（演示用）
        if (fail) {
            // 故意抛出异常，演示 OpenTelemetry 的异常捕获和 Span 错误标记
            throw new RuntimeException("模拟业务异常 - 演示错误追踪");
        }
        return Map.of("status", "请求成功，未触发错误");
    }
}
