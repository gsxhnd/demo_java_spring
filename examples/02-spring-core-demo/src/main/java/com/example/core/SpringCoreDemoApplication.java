package com.example.core;

import com.example.core.business.UserBusiness;
import com.example.core.config.AppConfig;
import com.example.core.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Spring Core 项目主入口 — 不使用 Spring Boot 的 SpringApplication，
 * 而是手动创建 IoC 容器，以此深入理解 Spring 底层核心机制。
 *
 * <h3>本类演示的 Spring 核心概念：</h3>
 * <ol>
 *   <li><b>IoC（Inversion of Control，控制反转）</b>：
 *       对象的创建、依赖装配、生命周期管理全部由 Spring 容器负责，
 *       开发者不再需要 new 对象，只需从容器中"拿"即可。</li>
 *   <li><b>ApplicationContext</b>：
 *       Spring IoC 容器的核心接口，代表"应用上下文"。它负责：
 *       <ul>
 *         <li>读取配置（XML / 注解 / Java Config）</li>
 *         <li>创建并管理所有 Bean 的单例实例</li>
 *         <li>完成依赖注入（DI）</li>
 *         <li>发布事件、解析消息等高级功能</li>
 *       </ul>
 *   </li>
 *   <li><b>AnnotationConfigApplicationContext</b>：
 *       ApplicationContext 的一种具体实现，用于基于 Java 注解配置的独立应用
 *       （非 Web 环境）。与之相对的是：
 *       <ul>
 *         <li>ClassPathXmlApplicationContext — 基于 XML 配置</li>
 *         <li>AnnotationConfigServletWebServerApplicationContext — Spring Boot Web 环境</li>
 *       </ul>
 *   </li>
 *   <li><b>getBean() 工作原理</b>：
 *       调用 context.getBean(Class) 时，Spring 在内部维护的 Bean 注册表中按类型查找。
 *       如果找到唯一的匹配 Bean，直接返回；如果有多个同类型的 Bean，抛出 NoUniqueBeanDefinitionException；
 *       如果找不到，抛出 NoSuchBeanDefinitionException。</li>
 *   <li><b>getBeanDefinitionCount() / getBeanDefinitionNames()</b>：
 *       用于查看容器中有多少 Bean 定义以及分别是什么，便于调试和理解容器内部状态。</li>
 * </ol>
 *
 * <h3>为什么不用 SpringApplication？</h3>
 * <p>SpringApplication.run() 是 Spring Boot 的入口，它会：
 * <ol>
 *   <li>创建内嵌的 Servlet Web 服务器（Tomcat / Jetty）</li>
 *   <li>加载 spring.factories 中的自动配置类</li>
 *   <li>执行 Environment 准备、Banner 打印等大量 Boot 特有逻辑</li>
 * </ol>
 * 这些"便利"会隐藏 Spring Core 容器的底层原理。对于一个学习 Core 机制的项目，
 * 直接使用 {@code AnnotationConfigApplicationContext} 能让学习者清晰地看到：
 * 容器是怎么创建的、Bean 是怎么注册的、依赖是怎么注入的。</p>
 *
 * <h3>执行流程概述：</h3>
 * <pre>
 * main()
 *  ├─ 创建 AnnotationConfigApplicationContext(AppConfig.class)
 *  │    ├─ 解析 @Configuration 和 @ComponentScan
 *  │    ├─ 扫描包，注册所有 @Component / @Service / @Repository
 *  │    ├─ 处理 @EnableAspectJAutoProxy → 为切面创建动态代理
 *  │    ├─ 实例化每个单例 Bean → 执行构造方法 → 注入依赖 → 调用 @PostConstruct
 *  │    └─ 容器就绪
 *  │
 *  ├─ Part 1: context.getBean(UserBusiness.class) → 演示从容器中获取 Bean
 *  ├─ Part 2: userBusiness.getUserInfo() → 演示依赖注入的实际效果
 *  ├─ Part 3: userBusiness.getUserInfo() → 演示 AOP 切面拦截
 *  ├─ Part 4: getBeanDefinitionCount() / getBeanDefinitionNames() → 查看容器内部状态
 *  └─ Part 5: UserService.getUser() → 演示 @PostConstruct / @PreDestroy 生命周期
 * </pre>
 *
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 */
@Slf4j
public class SpringCoreDemoApplication {

    /**
     * 程序主入口。
     * 按照五个部分依次演示 Spring Core 的核心功能：
     * IoC 容器创建、Bean 获取、依赖注入、AOP 拦截、Bean 生命周期。
     *
     * @param args 命令行参数（本示例未使用）
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("    Spring Core 项目 - IoC、Bean、DI、AOP");
        System.out.println("========================================\n");

        /*
         * 【核心】创建 Spring IoC 容器：
         *
         * AnnotationConfigApplicationContext 接受一个配置类作为参数，
         * 在构造方法中完成以下工作：
         *   1. 解析 AppConfig 上的 @Configuration、@ComponentScan、@EnableAspectJAutoProxy
         *   2. 根据 @ComponentScan 指定的包路径扫描所有组件
         *   3. 实例化所有单例 Bean（并完成依赖注入）
         *   4. 为满足 AOP 切入点的 Bean 创建代理
         *   5. 调用所有 Bean 的 @PostConstruct 初始化方法
         *
         * 构造函数返回时，容器已经完全就绪，所有 Bean 都可以直接使用。
         */
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        log.info("\n===== Spring 容器初始化完成 =====\n");

        // 演示 1: 从容器中获取 Bean（IoC 的核心使用方式）
        demonstrateBeansAndDI(context);

        // 演示 2: 验证依赖注入是否正常工作
        demonstrateDependencyInjection(context);

        // 演示 3: 查看 AOP 切面如何拦截方法调用
        demonstrateAOP(context);

        // 演示 4: 观察容器内部有哪些 Bean
        demonstrateContainer(context);

        // 演示 5: 展示 @PostConstruct 和 @PreDestroy 的生命周期回调
        demonstrateBeanLifecycle(context);

        System.out.println("\n========================================");
        System.out.println("              演示完成");
        System.out.println("========================================");
    }

    /**
     * 演示 1：从 IoC 容器中获取 Bean。
     *
     * <p>通过 {@code context.getBean(UserBusiness.class)} 按类型获取 Bean。
     * 这是 IoC 容器最基础的使用方式：不需要 new UserBusiness()，
     * 只需从容器中"取"即可，容器会返回一个依赖已完全注入的、可直接使用的对象。</p>
     *
     * <p>容器内部流程：Spring 根据 UserBusiness.class 类型，在自己的 Bean 注册表
     * （singletonObjects Map）中查找匹配项，找到后直接返回。如果找不到唯一的匹配 Bean，
     * 会根据情况抛出 NoUniqueBeanDefinitionException 或 NoSuchBeanDefinitionException。</p>
     *
     * @param context Spring IoC 容器（应用上下文）
     */
    private static void demonstrateBeansAndDI(ApplicationContext context) {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║        Part 1: Bean 获取与 DI         ║");
        System.out.println("╚═══════════════════════════════════════╝");

        // 【核心】从容器中按类型获取 Bean — 这是 IoC 容器最常用的操作
        // Spring 返回的是完成了所有依赖注入的"成品"对象，无需手动组装
        UserBusiness userBusiness = context.getBean(UserBusiness.class);

        log.info("\n✓ 成功从容器获取 UserBusiness Bean");
        log.info("✓ UserBusiness 的依赖（UserRepository）已自动注入");
    }

    /**
     * 演示 2：依赖注入的实际效果。
     *
     * <p>调用 UserBusiness 的业务方法时，其内部的 userRepository 字段已经被
     * Spring 通过构造器注入了 UserRepositoryImpl 实例，因此可以直接使用，
     * 不会出现 NPE（NullPointerException）。</p>
     *
     * <p>这就是 DI（Dependency Injection，依赖注入）的核心价值：
     * 调用方完全不需要知道依赖对象是怎么创建的、存在哪里，
     * 只需要声明"我需要一个 UserRepository"，Spring 会自动提供。</p>
     *
     * @param context Spring IoC 容器
     */
    private static void demonstrateDependencyInjection(ApplicationContext context) {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║        Part 2: 依赖注入演示           ║");
        System.out.println("╚═══════════════════════════════════════╝");

        UserBusiness userBusiness = context.getBean(UserBusiness.class);

        log.info("\n调用业务方法 - 将触发构造器注入的依赖");

        // 调用 getUserInfo 方法 — 内部会调用注入的 UserRepository.findById()
        // 如果依赖注入失败，这里会抛出 NPE
        Object userInfo = userBusiness.getUserInfo("user123");
        log.info("获取用户信息结果: {}", userInfo);

        // 调用 createUser 方法 — 同样依赖注入的 UserRepository
        userBusiness.createUser("user456", "userData");
    }

    /**
     * 演示 3：AOP 切面拦截方法调用。
     *
     * <p>当调用 UserBusiness 的方法时，由于 UserBusiness 在 com.example.core.business 包下，
     * 满足 LoggingAspect 的切入点表达式 {@code execution(public * com.example.core.business..*(..))}，
     * 因此 Spring 的 AOP 代理会在方法执行前后织入切面逻辑。</p>
     *
     * <p>你会在日志中看到以下通知按序输出：</p>
     * <ol>
     *   <li>@Around（前置部分）</li>
     *   <li>@Before</li>
     *   <li>目标方法 getUserInfo() 执行</li>
     *   <li>@AfterReturning（如果正常返回）或 @AfterThrowing（如果抛异常）</li>
     *   <li>@After</li>
     *   <li>@Around（后置部分，记录耗时）</li>
     * </ol>
     *
     * <p>注意：这里用 try-catch 包裹是因为 UserBusiness.getUserInfo("user789")
     * 可能触发某些异常场景，避免程序中断。</p>
     *
     * @param context Spring IoC 容器
     */
    private static void demonstrateAOP(ApplicationContext context) {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║        Part 3: AOP 切面演示           ║");
        System.out.println("╚═══════════════════════════════════════╝");

        // 从容器中获取的是经过 AOP 代理增强的 UserBusiness 对象
        // 代理对象内部持有真实的 UserBusiness 实例，并在方法调用前后插入切面逻辑
        UserBusiness userBusiness = context.getBean(UserBusiness.class);

        log.info("\n调用方法 - 将触发各种 AOP 通知");

        try {
            // 调用此方法将触发 LoggingAspect 中所有类型的通知
            userBusiness.getUserInfo("user789");
        } catch (Exception e) {
            // 捕获可能的业务异常，防止程序中断
            log.error("业务异常捕获", e);
        }
    }

    /**
     * 演示 4：查看 Spring IoC 容器的内部状态。
     *
     * <p>通过两个方法观察容器中注册了哪些 Bean：</p>
     * <ul>
     *   <li>{@code getBeanDefinitionCount()} — 返回容器中注册的 Bean 定义总数
     *       （包括 Spring 框架内部的 Bean，不仅仅是项目中的 Bean）</li>
     *   <li>{@code getBeanDefinitionNames()} — 返回所有 Bean 的名称数组
     *       （即 @Component 等注解标注的类的默认名称：类名首字母小写）</li>
     * </ul>
     *
     * <p>这里只打印了包名以 "com.example" 开头的 Bean，
     * 方便聚焦在项目自身的组件上，过滤掉 Spring 框架的内部 Bean。</p>
     *
     * @param context Spring IoC 容器
     */
    private static void demonstrateContainer(ApplicationContext context) {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║      Part 4: Spring 容器功能演示      ║");
        System.out.println("╚═══════════════════════════════════════╝");

        // 获取容器中的 Bean 定义总数（包含 Spring 自身和项目中的全部 Bean）
        log.info("\n容器中 Bean 总数: {}", context.getBeanDefinitionCount());

        // 获取所有 Bean 的名称列表
        String[] beanNames = context.getBeanDefinitionNames();
        log.info("所有 Bean 名称:");

        // 遍历并打印项目中自己的 Bean（包名以 "com.example" 开头）
        for (String beanName : beanNames) {
            if (beanName.startsWith("com.example")) {
                Object bean = context.getBean(beanName);
                log.info("  - {}: {}", beanName, bean.getClass().getSimpleName());
            }
        }
    }

    /**
     * 演示 5：Spring Bean 的生命周期（@PostConstruct 与 @PreDestroy）。
     *
     * <p>UserServiceImpl 中定义了 @PostConstruct 和 @PreDestroy 方法：</p>
     * <ul>
     *   <li>@PostConstruct 标注的 init() 方法已经在容器启动时
     *       （即前面 new AnnotationConfigApplicationContext(AppConfig.class) 时）
     *       自动被调用过了。这就是为什么你在控制台日志中会看到
     *       "3. @PostConstruct 初始化方法被调用" 的消息。</li>
     *   <li>@PreDestroy 标注的 destroy() 方法会在这个 main 方法结束时
     *       （JVM 退出导致容器关闭）自动被调用。
     *       你也可以显式调用 {@code ((ConfigurableApplicationContext) context).close()}
     *       来手动触发销毁过程。</li>
     * </ul>
     *
     * <p>完整的生命周期顺序回顾：</p>
     * <pre>
     *   构造方法 → 依赖注入 → @PostConstruct → Bean 就绪（使用中）→ @PreDestroy
     * </pre>
     *
     * @param context Spring IoC 容器
     */
    private static void demonstrateBeanLifecycle(ApplicationContext context) {
        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║    Part 5: Bean 生命周期演示          ║");
        System.out.println("╚═══════════════════════════════════════╝");

        // 获取 UserService Bean 并调用其方法 — 此时 @PostConstruct 已经执行过
        UserService userService = context.getBean(UserService.class);
        log.info("调用 UserService.getUser: {}", userService.getUser("user123"));
        userService.saveUser("user456", "{\"name\":\"John\"}");

        // @PostConstruct 已在容器启动时自动调用（你可以在之前的日志中看到）
        // @PreDestroy 将在容器关闭时（如 applicationContext.close() 或 JVM 退出时）自动调用
        log.info("Bean 生命周期演示完成（@PreDestroy 将在关闭时触发）");
    }
}
