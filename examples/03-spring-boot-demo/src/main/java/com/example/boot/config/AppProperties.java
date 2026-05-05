package com.example.boot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 应用配置属性类 — 演示 {@code @ConfigurationProperties} 类型安全的配置绑定。
 *
 * <h2>@ConfigurationProperties 注解详解</h2>
 * <p>
 * {@code @ConfigurationProperties(prefix = "app")} 指示 Spring Boot 从配置源
 * （application.yml、application.properties、环境变量、命令行参数等）中读取
 * 以 {@code "app"} 为前缀的所有属性，并自动映射到本类的同名字段上。
 * </p>
 *
 * <h3>属性映射规则（Relaxed Binding 宽松绑定）</h3>
 * <p>
 * Spring Boot 支持多种命名风格（kebab-case、camelCase、underscore_notation、
 * UPPER_CASE）之间的自动转换。例如以下写法等价且都能映射到 {@code contextPath} 字段：
 * </p>
 * <ul>
 *   <li>{@code app.server.contextPath}（camelCase）</li>
 *   <li>{@code app.server.context-path}（kebab-case）</li>
 *   <li>{@code app.server.context_path}（snake_case）</li>
 *   <li>{@code APP_SERVER_CONTEXTPATH}（大写）</li>
 * </ul>
 *
 * <h3>嵌套映射</h3>
 * <p>
 * 属性支持嵌套结构。YAML 的层级结构与 Java 内部类的层级结构自然对应：
 * </p>
 * <pre>
 * app:
 *   server:
 *     host: localhost    →  AppProperties.Server.host
 *     port: 8080         →  AppProperties.Server.port
 * </pre>
 *
 * <h2>@Component 的作用</h2>
 * <p>
 * 将本类注册为 Spring 容器管理的 Bean（组件）。只有注册到容器后，
 * Spring 才会对其执行 @ConfigurationProperties 的绑定逻辑。
 * 替代方案：在任意 @Configuration 类上使用 {@code @EnableConfigurationProperties(AppProperties.class)}
 * 显式注册，效果相同。
 * </p>
 *
 * <h2>@ConfigurationProperties vs @Value 对比</h2>
 * <p>
 * 这是 Spring Boot 中两种主流的配置注入方式，各有适用场景：
 * </p>
 * <table border="1">
 *   <tr><th>特性</th><th>@ConfigurationProperties</th><th>@Value</th></tr>
 *   <tr><td>绑定方式</td><td>基于 JavaBean 字段绑定，类型安全</td><td>基于 SpEL 表达式，逐字段注入</td></tr>
 *   <tr><td>嵌套对象</td><td>天然支持（如 server.host）</td><td>不支持（需逐个字段注入）</td></tr>
 *   <tr><td>批量绑定</td><td>一组相关属性一次绑定</td><td>需逐个 @Value 声明</td></tr>
 *   <tr><td>宽松绑定</td><td>完整支持</td><td>仅在属性名精确匹配时起作用</td></tr>
 *   <tr><td>校验</td><td>可配合 @Validated + Jakarta Validation 校验</td><td>无内置校验</td></tr>
 *   <tr><td>适用场景</td><td>一组相关的结构化配置（如数据源、缓存）</td><td>单个零散配置值</td></tr>
 * </table>
 *
 * <h2>@Data 注解</h2>
 * <p>
 * Lombok 提供的注解，编译时自动生成以下方法：
 * getter、setter、toString()、equals()、hashCode()、以及所有 final 字段的构造器。
 * 对于 @ConfigurationProperties 而言，setter 是关键 —— Spring 通过反射调用
 * setter 方法完成属性值的注入（字段的 setter 依赖 @Data 生成的 setName/setVersion 等）。
 * </p>
 *
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @see org.springframework.beans.factory.annotation.Value
 */
@Data
// @Component 将本类注册为 Spring Bean。只有这样，Spring 容器才会管理它，
// 并且 @ConfigurationProperties 的属性绑定才会被触发
@Component
// prefix = "app" 表示绑定 application.yml 中 app.* 下的所有配置项
// 启用这个注解后，IDE 的 spring-boot-configuration-processor 会提供自动补全提示
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * 应用名称。
     * 对应 application.yml 中的 {@code app.name}。
     * 默认值 "Spring Boot Demo"，当 YAML 中未配置时使用。
     */
    private String name = "Spring Boot Demo";

    /**
     * 应用版本号。
     * 对应 application.yml 中的 {@code app.version}。
     */
    private String version = "1.0.0";

    /**
     * 应用描述信息。
     * 对应 application.yml 中的 {@code app.description}。
     */
    private String description = "Spring Boot Basics Demo";

    /**
     * 服务器子配置 — 嵌套映射示例。
     * 对应 application.yml 中的 {@code app.server} 节点。
     * 注意：嵌套对象需要初始化（new Server()），否则 Spring 无法对 null 设置子属性。
     */
    private Server server = new Server();

    /**
     * 服务器配置内部类。
     * 演示 @ConfigurationProperties 如何将 YAML 层级结构映射到嵌套 Java 对象。
     * 使用 @Data 来自动生成 getter/setter。
     */
    @Data
    public static class Server {
        /**
         * 服务器主机地址，对应 {@code app.server.host}。
         */
        private String host = "localhost";

        /**
         * 服务器监听端口，对应 {@code app.server.port}。
         */
        private int port = 8080;

        /**
         * Servlet 上下文路径，对应 {@code app.server.contextPath}。
         * Spring Boot 的宽松绑定允许使用 camelCase 写法映射到 YAML 中的 contextPath。
         */
        private String contextPath = "/app";
    }
}
