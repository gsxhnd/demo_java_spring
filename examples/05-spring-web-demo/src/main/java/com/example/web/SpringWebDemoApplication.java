package com.example.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Web 开发项目主入口类。
 *
 * <h2>核心概念：Spring Boot 启动流程</h2>
 * <ol>
 *   <li><b>@SpringBootApplication</b> — 这是一个组合注解，等价于同时标注了三个注解：
 *     <ul>
 *       <li><b>@Configuration</b>：标记这是一个 Spring 配置类，等价于一个 XML 配置文件</li>
 *       <li><b>@EnableAutoConfiguration</b>：启用 Spring Boot 的自动配置机制，根据 classpath 中的 jar 依赖自动配置项目</li>
 *       <li><b>@ComponentScan</b>：自动扫描当前包及其子包下的 Spring 组件（@Component, @Service, @Controller, @Repository 等），并注册到 IoC 容器中</li>
 *     </ul>
 *   </li>
 *   <li><b>SpringApplication.run()</b> — 启动 Spring Boot 应用，核心工作包括：
 *     <ul>
 *       <li>创建 ApplicationContext（IoC 容器）</li>
 *       <li>自动配置嵌入式 Web 服务器（默认 Tomcat）</li>
 *       <li>扫描并注册所有 Bean</li>
 *       <li>启动内嵌服务器，监听端口（默认 8080）</li>
 *     </ul>
 *   </li>
 * </ol>
 *
 * <h2>@Slf4j</h2>
 * <p>Lombok 注解，在编译时自动为类生成一个名为 <code>log</code> 的 SLF4J Logger 静态字段，
 * 省去手动写 <code>private static final Logger log = LoggerFactory.getLogger(...)</code> 的样板代码。</p>
 *
 * <h2>OpenAPI / Swagger 配置</h2>
 * <p>本类中通过 <code>@Bean</code> 方法注册了一个 OpenAPI Bean，用于自定义 Swagger UI 页面上显示的 API 文档信息。
 * SpringDoc（Spring Boot 的 OpenAPI 实现）会自动扫描项目中的 @RestController，生成对应的 API 文档。
 * 访问地址：<code>http://localhost:8080/swagger-ui.html</code></p>
 *
 * <p>演示内容：</p>
 * <ul>
 *   <li>REST Controller 与路由映射</li>
 *   <li>请求参数处理 (@PathVariable, @RequestBody)</li>
 *   <li>响应处理与 DTO 设计模式</li>
 *   <li>全局异常处理 (@ControllerAdvice + @ExceptionHandler)</li>
 *   <li>参数校验 (Jakarta Bean Validation)</li>
 *   <li>API 文档 (Swagger UI / OpenAPI 3.0)</li>
 * </ul>
 *
 * @author Spring Demo Team
 */
@Slf4j
@SpringBootApplication
public class SpringWebDemoApplication {

    /**
     * 应用主方法——Java 程序的唯一入口点。
     * <p>JVM 通过 <code>public static void main(String[] args)</code> 来启动程序，
     * SpringApplication.run 负责初始化 Spring 上下文并启动内嵌 Web 服务器。</p>
     */
    public static void main(String[] args) {
        log.info("========================================");
        log.info("   Spring Web 开发项目 - 启动应用");
        log.info("========================================");

        /*
         * SpringApplication.run()：
         * 第一个参数是 @SpringBootApplication 标注的类（告诉 Spring "从这个配置类启动"），
         * 第二个参数是命令行参数（args），返回的是 ConfigurableApplicationContext。
         */
        SpringApplication.run(SpringWebDemoApplication.class, args);

        log.info("========================================");
        log.info("   Spring Boot 应用启动完成");
        log.info("   API 文档: http://localhost:8080/swagger-ui.html");
        log.info("   OpenAPI JSON: http://localhost:8080/v3/api-docs");
        log.info("========================================");
    }

    /**
     * 自定义 OpenAPI 配置 Bean。
     *
     * <h2>@Bean 注解说明</h2>
     * <p>@Bean 用于方法级别，表示该方法的返回值会被 Spring IoC 容器管理为一个 Bean。
     * 与 @Component 不同，@Bean 通常用在 @Configuration 类中的方法上。
     * SpringDoc 框架会自动检测容器中是否有 OpenAPI 类型的 Bean，如果有则使用自定义配置替代默认配置。</p>
     *
     * <h2>OpenAPI 3.0 对象模型</h2>
     * <p>这里使用了 OpenAPI 的构建器模式来创建 API 文档的元信息：
     * {@link Info} 对象包含标题(title)、版本(version)、描述(description)、
     * 联系人(Contact) 和许可协议(License) 信息。
     * 这些信息会显示在 Swagger UI 页面的顶部。</p>
     *
     * @return OpenAPI 对象，包含自定义的 API 文档元信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Web Demo API")
                        .version("1.0.0")
                        .description("Spring Web 开发示例 - RESTful API 文档")
                        .contact(new Contact()
                                .name("Spring Demo Team")
                                .url("https://example.com")
                                .email("info@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
