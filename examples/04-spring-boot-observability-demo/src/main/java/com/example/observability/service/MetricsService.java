package com.example.observability.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 指标服务 —— 演示 Micrometer 核心指标类型和 OpenTelemetry Observation 的手动/声明式创建。
 *
 * <h2>Micrometer 是什么？</h2>
 * Micrometer 是 JVM 生态的"指标门面（Metrics Facade）"，类似于 SLF4J 对日志的作用：
 * 它提供统一的 API 来创建和管理指标，底层可以对接多种监控系统（Prometheus、Datadog、InfluxDB 等）。
 * 本项目引入 spring-boot-starter-opentelemetry 后，Micrometer 指标会自动桥接到 OpenTelemetry，
 * 实现 Metrics → OTel 的统一导出。
 *
 * <h2>Micrometer 三大核心指标类型</h2>
 *
 * <h3>1. Counter（计数器）</h3>
 * 只增不减的累加器。典型使用场景：
 * <ul>
 *   <li>HTTP 请求总数</li>
 *   <li>错误发生次数</li>
 *   <li>订单处理数量</li>
 *   <li>消息队列消费条数</li>
 * </ul>
 * Counter 的基础值可以被 Prometheus 等系统转换为 rate（每秒请求数）等派生指标。
 *
 * <h3>2. Timer（计时器）</h3>
 * 用于测量操作的持续时间和调用频率。Timer 会自动记录：
 * <ul>
 *   <li>count：被计时操作的次数（相当于内嵌了一个 Counter）</li>
 *   <li>totalTime：所有操作的总耗时</li>
 *   <li>max：单次操作的最大耗时</li>
 *   <li>percentiles / histogram：延迟分布（需要额外配置 DistributionStatisticConfig）</li>
 * </ul>
 * 典型场景：HTTP 请求延迟、数据库查询耗时、RPC 调用耗时。
 *
 * <h3>3. Gauge（仪表/瞬时值）</h3>
 * 代表一个可以任意上下波动的瞬时值。与 Counter 不同，Gauge 的值不是 Monotonic（单调递
 * 增/减）的。
 * 典型场景：
 * <ul>
 *   <li>当前活跃连接数</li>
 *   <li>线程池活跃线程数</li>
 *   <li>JVM 堆内存使用量</li>
 *   <li>队列长度</li>
 * </ul>
 * 本项目使用 {@link AtomicInteger} 配合 {@link MeterRegistry#gauge(String, Object)} 实现 Gauge。
 *
 * <h2>MeterRegistry 是什么？</h2>
 * MeterRegistry 是 Micrometer 的核心注册表，所有指标（Meter）都注册到这个注册表中。
 * Spring Boot 自动创建了一个 CompositeMeterRegistry（组合注册表），可以同时注册到多个后端。
 * 调用 registry.gauge() / Counter.builder().register(registry) 等方法即可将指标添加进去。
 *
 * <h2>ObservationRegistry 是什么？</h2>
 * ObservationRegistry 是 Micrometer Observation 模块的核心注册表（与 MeterRegistry 不同！）。
 * 它负责管理 Observation（观察）的生命周期。在 Spring Boot + OpenTelemetry 的环境下：
 * <ol>
 *   <li>Observation 创建时会生成一个 Span（OpenTelemetry Span）</li>
 *   <li>Observation 携带的 KeyValue 会作为 Span 的 Attribute</li>
 *   <li>Observation 启动/停止会产生 Span Event</li>
 *   <li>Observation 会被注册到 OTel 的 SpanProcessor 中，最终导出到配置的 Exporter</li>
 * </ol>
 *
 * <h2>Trace、Metric、Log 三者如何关联？</h2>
 * <pre>
 * ┌──────────────────────────────────────────────────────┐
 * │  Trace（链路追踪）                                     │
 * │  traceId: abc123                                      │
 * │  ├── Span: GET /api/hello  [spanId: 001]             │
 * │  │   ├── Span: api.hello  [spanId: 002]              │
 * │  │   │   └── Span: metrics.record-hello  [spanId:003]│
 * │  │   └── Metrics: demo.hello.requests Counter += 1   │
 * │  └── Logs: @timestamp trace.id=abc123 span.id=001    │
 * └──────────────────────────────────────────────────────┘
 * </pre>
 * 三者通过 traceId / spanId 关联：日志中自动注入当前 Span 的 traceId 和 spanId（ECS 格式），
 * 指标（Metrics）通过 Exemplar（示例）机制关联到具体的 Trace。
 *
 * <h2>spring-boot-starter-opentelemetry 的自动配置</h2>
 * 引入该 Starter 后，Spring Boot 自动完成以下配置：
 * <ul>
 *   <li>自动创建 OpenTelemetry（SdkTracerProvider、SdkMeterProvider、SdkLoggerProvider）实例</li>
 *   <li>将 Micrometer MeterRegistry 桥接到 OTel Metrics</li>
 *   <li>将 Micrometer ObservationRegistry 桥接到 OTel Tracing</li>
 *   <li>根据 application.yml 中的 otel.* 配置设置 Exporter（本项目中为 logging exporter）</li>
 *   <li>自动为 Web 请求创建 HTTP Server Span</li>
 *   <li>将 SLF4J/MDC 中的 traceId/spanId 注入到日志输出</li>
 * </ul>
 */
@Slf4j
@Service
public class MetricsService {

    /**
     * 示例 Counter：统计 /api/hello 接口的调用次数。
     *
     * {@link Counter#builder(String)} 创建 Counter 构建器：
     * <ul>
     *   <li>name 参数："demo.hello.requests" 是指标名称（在 Prometheus 中会加 _total 后缀）</li>
     *   <li>.description()：指标描述，在 /actuator/metrics 端点中可见</li>
     *   <li>.tag()：附加标签（Tag），用于多维度区分指标，如区分不同接口的请求</li>
     *   <li>.register(meterRegistry)：将 Counter 注册到 MeterRegistry 并返回</li>
     * </ul>
     */
    private final Counter helloRequestCounter;

    /**
     * 示例 Counter：统计慢请求的调用次数。
     * Tag "type=slow" 可以用于在监控看板中按 type 分组聚合。
     */
    private final Counter slowRequestCounter;

    /**
     * 示例 Counter：统计错误模拟接口的调用次数。
     * 注意：这里的"错误"包含正常请求（fail=false）——仅为演示 Counter 用法。
     */
    private final Counter errorRequestCounter;

    /**
     * 示例 Gauge：当前活跃连接数。
     *
     * {@link AtomicInteger} 是线程安全的整数包装类，基于 CAS（Compare-And-Swap）实现。
     * 在多线程环境下，保证 value++ 操作的原子性，适合作为 Gauge 的数据源。
     *
     * Gauge 与 Counter 的核心区别：
     * <ul>
     *   <li>Gauge：可增可减，反映"当前值"（如连接数=5 → 10 → 3）</li>
     *   <li>Counter：只增不减，反映"累计值"（如总请求数=100 → 200 → 500）</li>
     * </ul>
     */
    private final AtomicInteger activeConnections;

    /**
     * 示例 Timer：记录请求处理耗时。
     * Timer 会自动统计 count（次数）、totalTime（总耗时）、max（最大耗时）。
     * 结合 Prometheus + Grafana 可以绘制 P50 / P95 / P99 延迟分布图。
     */
    private final Timer requestTimer;

    /**
     * ObservationRegistry（观察注册表），用于手动创建 Observation。
     * 与上面使用的 MeterRegistry 不同，ObservationRegistry 侧重于 Trace/Spans 的管理，
     * 而 MeterRegistry 侧重于 Metrics 的管理。
     */
    private final ObservationRegistry observationRegistry;

    /**
     * 构造器：通过 Spring 依赖注入获取 MeterRegistry 和 ObservationRegistry，
     * 并在构造阶段完成所有自定义指标的创建和注册。
     *
     * <h2>为什么在构造器中注册指标？</h2>
     * 指标需要在应用启动时一次性创建，而非每次请求都调用 builder。
     * Counter/Timer/Gauge 都是"长期存活的对象"（Long-lived），
     * 创建后通过 increment() / record() 等方法来更新值。
     *
     * @param meterRegistry     Micrometer 的指标注册表（Spring Boot 自动注入）
     * @param observationRegistry Micrometer Observation 的注册表（Spring Boot + OTel Starter 自动注入）
     */
    public MetricsService(MeterRegistry meterRegistry, ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;

        // ---- Counter（计数器）注册 ----
        // Counter.builder 创建构建器 → .tag() 添加维度标签 → .register() 注册到 Micrometer
        this.helloRequestCounter = Counter.builder("demo.hello.requests")
                .description("Number of /api/hello requests")
                .tag("type", "hello") // Tag 是 Prometheus label 概念：demo_hello_requests_total{type="hello"}
                .register(meterRegistry);

        this.slowRequestCounter = Counter.builder("demo.slow.requests")
                .description("Number of slow operation requests")
                .tag("type", "slow")
                .register(meterRegistry);

        this.errorRequestCounter = Counter.builder("demo.error.requests")
                .description("Number of error simulation requests")
                .tag("type", "error")
                .register(meterRegistry);

        // ---- Gauge（瞬时值）注册 ----
        // MeterRegistry#gauge(String, T) 是 Gauge 的快捷创建方法。
        // 第一个参数是指标名称，第二个参数是实现 Number 接口的对象（AtomicInteger 满足）。
        // Micrometer 会定期调用 activeConnections.get() 来获取当前值。
        this.activeConnections = new AtomicInteger(0);
        meterRegistry.gauge("demo.active.connections", this.activeConnections);

        // ---- Timer（计时器）注册 ----
        this.requestTimer = Timer.builder("demo.request.duration")
                .description("Request processing duration")
                .register(meterRegistry);

        log.info("MetricsService 初始化完成，所有自定义指标已注册");
    }

    /**
     * 记录 Hello 请求指标 —— 演示手动创建 Observation（子 Span）。
     *
     * <h2>Observation.createNotStarted() 详解</h2>
     * 与声明式 @Observed 注解不同，这里使用<b>编程式</b>方式创建 Observation：
     * <ol>
     *   <li>{@link Observation#createNotStarted(String, ObservationRegistry)}：
     *       创建一个"尚未启动"的 Observation 对象，参数为名称和注册表</li>
     *   <li>{@code .lowCardinalityKeyValue("component", "MetricsService")}：
     *       为 Observation 添加"低基数（Low Cardinality）"的键值对。
     *       <b>低基数 vs 高基数：</b>
     *       <ul>
     *         <li>低基数（low cardinality）：值的种类有限（如 HTTP 方法 GET/POST、服务名），
     *             适合作为 Span 属性或 Metric Tag</li>
     *         <li>高基数（high cardinality）：值的种类无限多（如 userId、requestId），
     *             只能作为 Span Event，不能作为 Metric Tag（会导致指标爆炸）</li>
     *       </ul>
     *   </li>
     *   <li>{@code .observe(Runnable)}：
     *       启动 Observation → 执行传入的 Runnable → 停止 Observation。
     *       这是最便捷的用法，确保 Observation 一定会被停止（类似 try-with-resources）。</li>
     * </ol>
     *
     * <h2>与当前 Trace 的关系</h2>
     * 如果在已有 Observation 的上下文中调用此方法（如 @Observed 标注的 hello() 方法内），
     * 新创建的 Observation 会自动成为当前 Span 的<b>子 Span</b>，形成嵌套的调用链。
     * 如果不在任何 Observation 上下文中，则会创建一条新的 Trace。
     */
    public void recordHelloRequest() {
        // Counter 指标 +1：每次调用都会累加
        helloRequestCounter.increment();

        // 手动创建一个 Observation（子 Span）
        // "metrics.record-hello" 是这个 Span 的名称，会在 Jaeger/控制台中显示
        Observation.createNotStarted("metrics.record-hello", observationRegistry)
                .lowCardinalityKeyValue("component", "MetricsService")
                .observe(() -> {
                    // 这个 lambda 内的代码是在 Observation 的 Scope 中执行的
                    // 此时日志输出会自动携带当前 Span 的 traceId 和 spanId
                    log.info("记录 Hello 请求指标（子 Span 中）");
                });
    }

    /**
     * 记录慢请求指标 —— 演示 @Observed 声明式 Observation 和 Timer 记录。
     *
     * <h2>@Observed 注解在此方法上的作用</h2>
     * 与 DemoController 中使用 @Observed 的原理完全相同：
     * Spring AOP 在方法执行前后创建/停止 Observation（即 Span）。
     * Span 的名称为 "metrics.record-slow"，上下文名称为 "记录慢请求指标"。
     *
     * <h2>Timer#record() 方法</h2>
     * 手动记录操作的耗时。这里传入 1 秒作为演示值。
     * 在生产环境中，Timer 通常配合 {@code Timer.Sample sample = Timer.start(registry)} 使用：
     * <pre>{@code
     * Timer.Sample sample = Timer.start(registry);
     * // ... 执行耗时操作 ...
     * sample.stop(Timer.builder("my.timer").register(registry));
     * }</pre>
     *
     * @see Observed
     */
    @Observed(name = "metrics.record-slow", contextualName = "记录慢请求指标")
    public void recordSlowRequest() {
        // 慢请求 Counter 指标 +1
        slowRequestCounter.increment();
        // 手动向 Timer 记录一次 1 秒的耗时（演示用，实际环境中应记录真实耗时）
        requestTimer.record(1, TimeUnit.SECONDS);
    }

    /**
     * 记录错误请求指标。
     * Counter 累加，没有任何 Observation 操作 —— 错误捕获由 Spring Boot 框架层自动处理：
     * 当异常抛出时，Spring 会将其记录到当前 Span 并标记 Span 状态为 ERROR。
     */
    public void recordErrorRequest() {
        errorRequestCounter.increment(); // Counter 累加：demo.error.requests{type="error"} += 1
    }

    /**
     * 模拟设置当前活跃连接数 —— 演示 Gauge 的用法。
     *
     * activeConnections 是一个 {@link AtomicInteger}，它已通过
     * {@code meterRegistry.gauge("demo.active.connections", this.activeConnections)} 注册为 Gauge。
     * Micrometer 会定时轮询（Pull 模式）activeConnections.get() 的值并上报。
     *
     * <h2>Pull vs Push 模式</h2>
     * <ul>
     *   <li><b>Pull（拉模式）</b>：Prometheus 定期访问 /actuator/prometheus 端点拉取指标。
     *       Gauge 的值在拉取时才通过 get() 获取，适合 Prometheus。</li>
     *   <li><b>Push（推模式）</b>：应用主动将指标推送到监控后端（如 Datadog、InfluxDB）。
     *       Micrometer 定期推送快照，Gauge 的值也是此时获取。</li>
     * </ul>
     * 本项目使用 OTel logging exporter，启动时就会发现"推送"的动作：指标值会输出到控制台。
     *
     * @param count 要模拟的活跃连接数
     */
    public void simulateActiveConnections(int count) {
        activeConnections.set(count);
    }
}
