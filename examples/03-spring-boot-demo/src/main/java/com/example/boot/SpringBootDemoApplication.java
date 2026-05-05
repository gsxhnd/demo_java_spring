package com.example.boot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 应用主入口类。
 *
 * <h2>核心注解 @SpringBootApplication 详解</h2>
 * <p>
 * {@code @SpringBootApplication} 是一个组合注解（元注解），它等价于同时标注了以下三个注解：
 * </p>
 * <ol>
 *   <li><b>@Configuration（标记为配置类）</b>：
 *       当前类可以作为 Spring 容器的一个配置源，允许在其中用 @Bean 方法显式声明 Bean。
 *       在 Spring Boot 中，主类通常不直接写 @Bean，但该能力保留以支持复杂场景。</li>
 *   <li><b>@EnableAutoConfiguration（启用自动配置）</b>：
 *       Spring Boot 的核心能力。它会根据 classpath 中引入的 jar 依赖（Starter 机制），
 *       自动装配（Auto-configuration）所需的 Bean。例如：引入了 spring-boot-starter-web，
 *       就会自动配置 DispatcherServlet、HttpMessageConverter、内嵌 Tomcat 等，无需
 *       任何 XML 或手动 @Bean 声明。</li>
 *   <li><b>@ComponentScan（组件扫描）</b>：
 *       自动扫描当前类所在包及其子包，发现并注册带有 @Component、@Service、@Repository、
 *       @Controller 等注解的类到 Spring 容器中。</li>
 * </ol>
 *
 * <h2>为什么放在 main 方法所在的类上？</h2>
 * <p>
 * 约定优于配置（Convention over Configuration）的体现。Spring Boot 推荐将主类放在
 * 根包（root package）下，这样 @ComponentScan 就会自动扫描所有子包中的组件，
 * 开发者无需手动指定扫描路径。本例中主类位于 com.example.boot，其子包 controller/、
 * service/、config/ 下的所有组件都会被自动发现和注册。
 * </p>
 *
 * <h2>@Slf4j 注解</h2>
 * <p>
 * Lombok 提供的注解，编译时自动生成一个名为 {@code log} 的 SLF4J Logger 静态字段，
 * 等同于手写 {@code private static final Logger log = LoggerFactory.getLogger(当前类名.class);}。
 * 配合 Spring Boot 默认集成的 Logback，可以直接使用 {@code log.info/debug/error()} 打印日志。
 * </p>
 *
 * <h2>SpringApplication.run() 执行流程</h2>
 * <p>
 * 这一行代码启动了整个 Spring Boot 应用，背后执行了以下关键步骤：
 * </p>
 * <ol>
 *   <li><b>创建 SpringApplication 实例</b>：
 *        通过构造器推断应用类型（Servlet / Reactive / 纯 Java），
 *        加载 META-INF/spring.factories 中的 ApplicationContextInitializer 和 ApplicationListener。</li>
 *   <li><b>准备 Environment</b>：
 *        创建并配置 ConfigurableEnvironment，加载 application.yml 等配置文件，
 *        设置 active profiles（如 --spring.profiles.active=dev）、
 *        命令行参数等，形成统一的配置源。</li>
 *   <li><b>创建 ApplicationContext（应用上下文）</b>：
 *        根据应用类型创建对应的 Spring 容器。对于 Web 应用，
 *        默认创建 AnnotationConfigServletWebServerApplicationContext。</li>
 *   <li><b>触发自动配置（Auto-Configuration）</b>：
 *        基于 @EnableAutoConfiguration 和 spring-boot-autoconfigure 模块，
 *        通过条件注解（@ConditionalOnClass、@ConditionalOnMissingBean 等）
 *        判断哪些 Bean 需要自动装配到容器中。</li>
 *   <li><b>启动内嵌 Web 服务器</b>：
 *        启动内嵌的 Tomcat / Jetty / Undertow，绑定端口（默认 8080），
 *        注册 DispatcherServlet 到 Servlet 容器。</li>
 *   <li><b>执行 CommandLineRunner / ApplicationRunner</b>：
 *        容器刷新完成后，回调所有实现了这些接口的 Bean，
 *        适合在应用启动后执行一次性初始化逻辑。</li>
 * </ol>
 *
 * @see SpringApplication
 * @see SpringBootApplication
 */
@Slf4j
@SpringBootApplication
public class SpringBootDemoApplication {

    /**
     * 应用启动的入口方法（Java SE 标准入口点）。
     *
     * @param args 命令行参数，会被传递给 Spring Boot 并合并到 Environment 配置中
     */
    public static void main(String[] args) {
        log.info("========================================");
        log.info("   Spring Boot Demo - 启动应用");
        log.info("========================================");

        // SpringApplication.run() 是 Spring Boot 的启动器（Bootstrap）
        // 参数1: primarySource — 主配置类，Spring 从这里读取 @SpringBootApplication 注解
        // 参数2: args — 命令行参数，可覆盖 application.yml 中的配置
        // 返回值: ConfigurableApplicationContext — Spring IoC 容器的最终形态
        SpringApplication.run(SpringBootDemoApplication.class, args);

        log.info("========================================");
        log.info("   Spring Boot 应用启动完成");
        log.info("   访问 http://localhost:8080/api/hello");
        log.info("========================================");
    }
}
