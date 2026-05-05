package com.example.boot.controller;

import com.example.boot.config.AppProperties;
import com.example.boot.service.BootstrapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 演示控制器 — 展示 Spring Boot Web 层（RESTful API）的核心用法。
 *
 * <h2>@RestController 注解详解</h2>
 * <p>
 * {@code @RestController} 是一个组合注解，等价于同时标注：
 * </p>
 * <ol>
 *   <li><b>@Controller</b>：将本类标记为 Spring MVC 的控制器（Handler），
 *       使 Spring 能够识别它并注册到请求映射表（HandlerMapping）中。</li>
 *   <li><b>@ResponseBody</b>：指示该控制器中所有方法的返回值都应直接序列化
 *       为 HTTP 响应体（JSON/XML），而不是解析为视图名（View Name）。
 *       序列化策略由 Spring Boot 自动配置的 HttpMessageConverter 决定，
 *       默认使用 Jackson 将对象转为 JSON。</li>
 * </ol>
 * <p>
 * <b>对比传统 @Controller</b>：传统 @Controller 的方法返回值默认被解释为
 * 视图名称（如 "index" → /templates/index.html）。如果仅想要 REST API，
 * @RestController 省去了在每个方法上加 @ResponseBody 的麻烦。
 * </p>
 *
 * <h2>@RequestMapping("/api") 详解</h2>
 * <p>
 * 类级别的 @RequestMapping 为该控制器中所有方法定义了一个公共的 URL 前缀。
 * 例如：本类中 {@code @GetMapping("/hello")} 的实际访问路径是 {@code GET /api/hello}。
 * 这样可以方便地对一组相关的 API 进行分组管理。
 * </p>
 *
 * <h2>构造函数注入（Constructor Injection）</h2>
 * <p>
 * 本类使用 Spring 推荐的最佳实践 —— 构造函数注入：
 * </p>
 * <ul>
 *   <li>依赖通过 {@code private final} 字段声明，确保不可变性和线程安全。</li>
 *   <li>Spring 在实例化本类时，会自动从容器中查找匹配类型的 Bean 并传入构造器。
 *       由于依赖是 final 的，编译器保证了它们在对象创建后不会被更改。</li>
 *   <li>相比字段注入（@Autowired on field），构造函数注入的优点：
 *       <ol>
 *         <li>依赖不可变（Immutable Dependencies）</li>
 *         <li>更容易进行单元测试（可以手动传入 mock 对象）</li>
 *         <li>可以在构造器中执行初始化逻辑</li>
 *         <li>避免依赖于 Spring 容器的字段注入（可以在容器外使用 new 创建）</li>
 *       </ol>
 *   </li>
 *   <li>当只有一个构造器时（如本类），Spring 会自动执行依赖注入，
 *       无需显式标注 {@code @Autowired}。</li>
 * </ul>
 *
 * <h2>@Value 注入单个配置值</h2>
 * <p>
 * 与 AppProperties 使用的 @ConfigurationProperties 不同，@Value 适合注入
 * 单个零散的配置项。语法格式：{@code @Value("${属性名:默认值}")}。
 * 冒号后的部分是默认值，当配置源中找不到对应属性时使用。
 * </p>
 *
 * @see RestController
 * @see AppProperties
 */
@Slf4j
// @RestController = @Controller + @ResponseBody
// 所有方法的返回值都会被 Jackson 自动序列化为 JSON 写入 HTTP 响应体
@RestController
// 为所有接口添加 /api 前缀，方便统一管理 API 路径
@RequestMapping("/api")
public class DemoController {

    // 通过构造函数注入 AppProperties Bean（类型安全的配置绑定对象）
    private final AppProperties appProperties;

    // 通过构造函数注入 BootstrapService Bean（业务服务层对象）
    private final BootstrapService bootstrapService;

    /**
     * 通过 @Value 注入单个配置项 — server.servlet.context-path。
     * <p>
     * 语法解释：
     * </p>
     * <ul>
     *   <li>{@code ${...}} — 表示这是一个属性占位符（Property Placeholder），
     *       Spring 会在 Environment 中查找该属性值。</li>
     *   <li>{@code :} — 冒号后为默认值。本例中默认值为空字符串 ""，
     *       表示如果 application.yml 中没有配置 server.servlet.context-path，
     *       则 contextPath 赋值为 ""。</li>
     * </ul>
     * <p>
     * <b>注意</b>：@Value 适合单个值的注入；对于一组相关配置，应优先使用
     * @ConfigurationProperties（见 AppProperties 类的说明）。
     * </p>
     */
    @Value("${server.servlet.context-path:}")
    private String contextPath;

    /**
     * 构造函数注入（Spring 推荐的依赖注入方式）。
     * <p>
     * Spring 在创建 DemoController 实例时，会自动从容器中找到 AppProperties
     * 和 BootstrapService 类型的 Bean 并传入此构造器。由于本类是单例的
     * （Spring 默认作用域），这些依赖只会在启动时注入一次。
     * </p>
     *
     * @param appProperties     应用配置属性（来自 @ConfigurationProperties 绑定）
     * @param bootstrapService  启动信息服务（提供启动日志打印功能）
     */
    public DemoController(AppProperties appProperties, BootstrapService bootstrapService) {
        this.appProperties = appProperties;
        this.bootstrapService = bootstrapService;
    }

    /**
     * 欢迎端点 — 演示最简单的 GET 接口。
     *
     * <p>
     * {@code @GetMapping("/hello")} 是一个快捷注解，等价于：
     * {@code @RequestMapping(method = RequestMethod.GET, value = "/hello")}。
     * 它将 HTTP GET 请求映射到当前方法。
     * </p>
     * <p>
     * 由于类上有 {@code @RequestMapping("/api")}，实际的访问 URL 是：
     * {@code GET http://localhost:8080/api/hello}
     * </p>
     * <p>
     * 返回值 {@code Map<String, Object>} 会被 Jackson 自动序列化为 JSON：
     * <pre>
     * {
     *   "message": "欢迎来到 Spring Boot 演示",
     *   "appName": "Spring Boot 演示应用",
     *   "version": "1.0.0"
     * }
     * </pre>
     * </p>
     *
     * @return 包含欢迎消息和应用基本信息的 Map，自动序列化为 JSON 响应
     */
    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "欢迎来到 Spring Boot 演示");
        // 通过 AppProperties 读取 application.yml 中 app.name 的配置值
        response.put("appName", appProperties.getName());
        // 通过 AppProperties 读取 application.yml 中 app.version 的配置值
        response.put("version", appProperties.getVersion());
        return response;
    }

    /**
     * 应用信息端点 — 演示返回嵌套 JSON 对象。
     *
     * <p>
     * 访问 URL：{@code GET http://localhost:8080/api/info}
     * </p>
     * <p>
     * 此方法展示如何将 AppProperties 中的嵌套配置（app.server.*）
     * 以 JSON 格式返回给客户端。返回的 JSON 结构如下：
     * </p>
     * <pre>
     * {
     *   "name": "Spring Boot 演示应用",
     *   "version": "1.0.0",
     *   "description": "...",
     *   "server": {
     *     "host": "localhost",
     *     "port": 8080,
     *     "contextPath": "/app"
     *   }
     * }
     * </pre>
     *
     * @return 包含完整应用信息的 Map，包括嵌套的服务器配置
     */
    @GetMapping("/info")
    public Map<String, Object> getAppInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", appProperties.getName());
        response.put("version", appProperties.getVersion());
        response.put("description", appProperties.getDescription());
        // Map.of() 创建一个不可变的 Map，用于构建嵌套的服务器信息对象
        // Spring Boot 默认的 Jackson 配置会将 Map 序列化为 JSON 对象
        response.put("server", Map.of(
            "host", appProperties.getServer().getHost(),
            "port", appProperties.getServer().getPort(),
            "contextPath", appProperties.getServer().getContextPath()
        ));
        return response;
    }

    /**
     * 启动日志端点 — 演示调用 Service 层方法。
     *
     * <p>
     * 访问 URL：{@code GET http://localhost:8080/api/startup}
     * </p>
     * <p>
     * 此方法调用 BootstrapService 的 printBootstrapInfo() 方法，
     * 将启动信息打印到控制台日志中，同时返回一个状态说明给客户端。
     * 展示了 Controller → Service 的分层调用模式。
     * </p>
     *
     * @return 包含操作状态的 Map，JSON 格式返回
     */
    @GetMapping("/startup")
    public Map<String, String> startup() {
        // 委托给 Service 层执行业务逻辑，Controller 只负责接收请求和返回响应
        bootstrapService.printBootstrapInfo();
        return Map.of("status", "启动信息已打印到日志");
    }
}
