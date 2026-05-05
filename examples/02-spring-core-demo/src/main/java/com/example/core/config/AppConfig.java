package com.example.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Spring 核心配置类 — 替代传统的 XML 配置文件，使用纯 Java 代码完成 Spring IoC 容器的配置。
 *
 * <h3>本类演示的 Spring 核心知识点：</h3>
 * <ol>
 *   <li><b>@Configuration</b>：标记一个类为"配置类"，等价于 Spring 的 XML 配置文件（如 applicationcontext.xml）。
 *       Spring 会使用 CGLIB 动态代理对该类生成子类，确保容器中每个 @Bean 方法返回的都是同一个单例对象。
 *       如果去掉 @Configuration，@Bean 方法之间的相互调用将不再走容器代理，而是普通的 Java 方法调用，可能破坏单例语义。</li>
 *   <li><b>@ComponentScan</b>：开启"组件扫描"，告诉 Spring 去哪些包（basePackages）下查找带有
 *       @Component / @Service / @Repository / @Controller 等注解的类，自动将它们注册为 Spring Bean。
 *       如果不指定 basePackages，默认扫描当前配置类所在的包及其子包。</li>
 *   <li><b>@EnableAspectJAutoProxy</b>：开启 AspectJ 自动代理，使 @Aspect 注解的切面类生效。
 *       底层原理：Spring 会为符合切点条件的目标 Bean 创建动态代理（JDK 动态代理或 CGLIB），
 *       在方法调用前后织入通知逻辑。如果没有这个注解，@Aspect 切面将不会被 Spring 处理。</li>
 * </ol>
 *
 * <h3>为什么在本项目中不使用 SpringApplication？</h3>
 * <p>SpringApplication.run() 是 Spring Boot 的启动方式，它内部封装了内嵌 Tomcat、自动配置等大量 Boot 特性。
 * 而本项目的目的是学习 Spring 核心（Core）容器的底层机制，因此直接使用 {@code AnnotationConfigApplicationContext}
 * 手动创建容器，这能让开发者更清晰地看到 IoC 容器的启动过程。</p>
 *
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ComponentScan
 * @see org.springframework.context.annotation.EnableAspectJAutoProxy
 */
@Slf4j
@Configuration                      // 【核心注解】声明这是一个 Spring 配置类，相当于过去的 XML 配置文件
@ComponentScan(basePackages = "com.example.core") // 【核心注解】组件扫描：自动发现 com.example.core 包下的所有 Bean
@EnableAspectJAutoProxy            // 【核心注解】启用 AOP：让 @Aspect 注解的切面类能够拦截目标方法
public class AppConfig {

    /**
     * Spring IoC 容器启动时会调用此构造方法创建 AppConfig 实例。
     * 该实例本身也是一个被容器管理的 Bean（配置类 Bean），
     * 其默认 Bean 名称遵循命名规则："类名首字母小写"（即 appConfig）。
     */
    public AppConfig() {
        log.info("AppConfig 被创建");
    }
}
