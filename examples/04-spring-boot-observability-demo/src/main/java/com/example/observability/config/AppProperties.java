package com.example.observability.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 自定义应用配置属性类。
 *
 * <h2>@ConfigurationProperties 工作原理</h2>
 * Spring Boot 会扫描 application.yml（或 application.properties）中以 "app" 为前缀的配置项，
 * 并自动将同名的属性值绑定（Binding）到这个 JavaBean 中。属性名采用"宽松绑定"规则（Relaxed Binding）：
 * camelCase（contextPath）、kebab-case（context-path）、下划线（context_path）均可匹配。
 *
 * <pre>
 * application.yml 示例：
 * app:
 *   name: Spring Boot 可观测性演示应用
 *   version: 1.0.0
 *   server:
 *     host: localhost
 *     port: 8080
 *     contextPath: /app
 * </pre>
 *
 * <h2>@Component 的作用</h2>
 * 将 AppProperties 注册为一个 Spring Bean，使其可以被其他组件通过构造器注入（Constructor Injection）引用。
 * 另一种常见做法是使用 @EnableConfigurationProperties(AppProperties.class) 在配置类上激活，
 * 而不需要 @Component —— 本项目采用更简洁的 @Component 方式。
 *
 * <h2>@Data 的作用</h2>
 * Lombok 注解，自动生成：getter/setter、toString()、equals()、hashCode()、以及一个包含所有
 * non-static、non-transient 字段的构造器。Spring 通过 setter 方法完成属性注入。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app") // 绑定 application.yml 中 app.* 的配置
public class AppProperties {

    /** 应用名称，默认值 "Spring Boot Observability Demo" */
    private String name = "Spring Boot Observability Demo";

    /** 版本号，默认值 "1.0.0" */
    private String version = "1.0.0";

    /** 应用描述，默认值 "Spring Boot Observability Demo" */
    private String description = "Spring Boot Observability Demo";

    /**
     * 服务器相关配置，以嵌套对象形式绑定 app.server.* 配置项。
     * Spring Boot 支持多层嵌套（递归绑定），不需要 @ConfigurationProperties 注解在 Server 上。
     */
    private Server server = new Server();

    /**
     * 静态内部类 —— 服务器配置。
     * 字段名称与 application.yml 中 app.server.* 的 key 名称对应。
     */
    @Data
    public static class Server {
        /** 服务器主机名，默认 localhost */
        private String host = "localhost";
        /** 服务器端口，默认 8080 */
        private int port = 8080;
        /** 上下文路径（Servlet Context Path），默认 /app */
        private String contextPath = "/app";
    }
}
