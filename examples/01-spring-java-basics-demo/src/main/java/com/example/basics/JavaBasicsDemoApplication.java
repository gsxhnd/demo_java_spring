package com.example.basics;

import com.example.basics.annotation.AnnotationProcessor;
import com.example.basics.lambda.LambdaExample;
import com.example.basics.model.UserService;
import com.example.basics.reflection.ReflectionExample;

/**
 * Java 基础项目的主入口类（非 Spring Boot 启动类，而是纯 Java SE 的 main 方法入口）。
 *
 * <h2>本类演示的核心概念</h2>
 * <ul>
 *   <li><b>纯 Java 应用启动：</b>通过 {@code public static void main(String[] args)} 方法启动程序，
 *       不依赖 Spring Boot 的 {@code SpringApplication.run()}。</li>
 *   <li><b>演示编排：</b>将三个独立的 Java 核心技术（注解、反射、Lambda）串联在一起逐一演示。</li>
 *   <li><b>静态方法调用：</b>通过静态 import 和直接类名调用工具方法，展示 Java 代码组织方式。</li>
 * </ul>
 *
 * <h2>运行流程</h2>
 * 程序按顺序执行三个部分：
 * <ol>
 *   <li>Part 1: 注解机制演示 — 展示自定义注解的定义、标注和处理</li>
 *   <li>Part 2: 反射机制演示 — 展示如何在运行时获取类的元信息并动态操作</li>
 *   <li>Part 3: Lambda 与函数式编程演示 — 展示 Lambda 表达式、方法引用、Stream API</li>
 * </ol>
 *
 * @author basics-demo
 */
public class JavaBasicsDemoApplication {

    /**
     * Java 程序的入口点。
     *
     * <p>
     * {@code main} 方法是 JVM 启动程序时调用的第一个方法。它的签名必须是：
     * {@code public static void main(String[] args)}，这是 Java 语言规范规定的固定格式。
     * </p>
     *
     * <p>
     * {@code args} 参数是从命令行传入的字符串数组，本示例中不使用命令行参数。
     * </p>
     *
     * @param args 命令行参数，通常用于传递程序启动配置
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("    Java 基础项目 - 注解、反射、Lambda");
        System.out.println("========================================");

        // Part 1: 注解演示 — 展示如何定义和使用自定义注解
        // 注解本身不会自动生效，需要通过反射来"读取"并"处理"
        demonstrateAnnotations();

        // Part 2: 反射演示 — 展示如何在运行时检查类的结构
        // 反射是 Java 框架（如 Spring）的基石，Spring 用反射来创建 Bean、注入依赖
        demonstrateReflection();

        // Part 3: Lambda 演示 — 展示 Java 8+ 的函数式编程能力
        // Lambda 让代码更简洁，配合 Stream API 可以声明式地处理集合
        demonstrateLambda();

        System.out.println("\n========================================");
        System.out.println("              演示完成");
        System.out.println("========================================");
    }

    /**
     * 演示 Java 注解（Annotation）的使用机制。
     *
     * <h2>演示内容</h2>
     * <ul>
     *   <li><b>类级别注解：</b>{@code @Component} 用于标记一个类为"组件"，
     *       类似于 Spring 中的 {@code @Component}。</li>
     *   <li><b>字段级别注解：</b>{@code @Inject} 用于标记需要注入的字段，
     *       类似于 Spring 中的 {@code @Autowired} 或 JSR-330 的 {@code @Inject}。</li>
     *   <li><b>方法级别注解：</b>{@code @Process} 用于标记需要特殊处理的方法，
     *       常用于 AOP（面向切面编程）场景。</li>
     * </ul>
     *
     * <h2>核心原理</h2>
     * <p>
     * 注解本身只是"标签"，不会自动执行任何逻辑。必须通过 <b>反射</b> 在运行时读取注解，
     * 并根据注解的值执行相应的处理逻辑。这也是 Spring 框架的核心工作原理——
     * Spring 容器启动时会扫描所有带 {@code @Component} 的类，读取注解信息，
     * 然后创建 Bean 并注入依赖。
     * </p>
     */
    private static void demonstrateAnnotations() {
        System.out.println("\n\n╔═══════════════════════════════════════╗");
        System.out.println("║          Part 1: 注解机制演示          ║");
        System.out.println("╚═══════════════════════════════════════╝");

        // 创建一个 UserService 实例
        // UserService 类上使用了 @Component、字段上使用了 @Inject、方法上使用了 @Process
        UserService userService = new UserService();

        // 处理类级别注解 — 扫描并解析 @Component 注解
        // 传入类的 Class 对象（.class 是 Java 中获取 Class 对象的方式之一）
        AnnotationProcessor.processComponentAnnotation(UserService.class);

        // 处理字段级别注解 — 扫描并解析所有字段上的 @Inject 注解
        AnnotationProcessor.processFieldAnnotation(UserService.class);

        // 处理方法级别注解 — 扫描并解析所有方法上的 @Process 注解
        // 注意：这里传入的是"实例对象"而非 Class 对象，因为 getDeclaredMethods() 可以从实例获取
        AnnotationProcessor.processMethodAnnotation(userService);
    }

    /**
     * 演示 Java 反射（Reflection）机制。
     *
     * <h2>什么是反射？</h2>
     * <p>
     * 反射是 Java 提供的一种能力，允许程序在 <b>运行时</b> 检查和操作类的结构，
     * 包括：类名、父类、接口、构造方法、字段、方法等。反射甚至可以绕过访问控制（private），
     * 通过 {@code setAccessible(true)} 来操作私有成员。
     * </p>
     *
     * <h2>反射的核心 API</h2>
     * <ul>
     *   <li>{@code Class<?>}：代表一个类的元数据，是反射的入口</li>
     *   <li>{@code Constructor<?>}：代表构造方法</li>
     *   <li>{@code Field}：代表字段（属性）</li>
     *   <li>{@code Method}：代表方法</li>
     *   <li>{@code getDeclaredXxx()}：获取该类自身声明的成员（不包括继承的）</li>
     *   <li>{@code getXxx()}：获取所有 public 成员（包括继承的）</li>
     * </ul>
     *
     * <h2>反射与 Spring 框架的关系</h2>
     * <p>
     * Spring 框架大量使用反射来实现 IoC（控制反转）和 DI（依赖注入）：
     * </p>
     * <ul>
     *   <li>Spring 启动时扫描所有带注解的类（注解扫描）</li>
     *   <li>通过反射创建这些类的实例（Bean 实例化）</li>
     *   <li>通过反射注入标记了 {@code @Autowired} 的字段（依赖注入）</li>
     *   <li>通过反射调用 {@code @PostConstruct} 等方法（生命周期回调）</li>
     * </ul>
     */
    private static void demonstrateReflection() {
        System.out.println("\n\n╔═══════════════════════════════════════╗");
        System.out.println("║          Part 2: 反射机制演示          ║");
        System.out.println("╚═══════════════════════════════════════╝");

        // 获取 UserService 类的 Class 对象
        // 有三种方式获取 Class 对象：
        // 1. 类名.class（编译期确定）
        // 2. 对象.getClass()（运行时确定）
        // 3. Class.forName("全限定类名")（动态加载）
        Class<?> clazz = UserService.class;

        try {
            // 步骤 1: 打印类的元信息（类名、父类、实现的接口）
            ReflectionExample.printClassMetadata(clazz);

            // 步骤 2: 打印所有构造方法（包括 private 的）
            // getDeclaredConstructors() 返回类自身声明的所有构造方法
            ReflectionExample.printConstructors(clazz);

            // 步骤 3: 打印所有字段（包括 private 的）
            // getDeclaredFields() 返回类自身声明的所有字段
            ReflectionExample.printFields(clazz);

            // 步骤 4: 打印所有方法（包括 private 的）
            // getDeclaredMethods() 返回类自身声明的所有方法
            ReflectionExample.printMethods(clazz);

            // 步骤 5: 通过反射动态创建对象（带参数构造方法）
            // 参数类型列表必须是精确匹配的，这里指定是 String.class（单参数 String 构造）
            Object instance = ReflectionExample.instantiateClass(
                clazz,
                new Class<?>[] {String.class},   // 构造参数类型
                new Object[] {"DynamicUserService"} // 构造参数值
            );

            // 步骤 6: 通过反射动态调用方法
            // 传入方法名、参数类型列表、参数值列表
            ReflectionExample.invokeMethod(
                instance,
                "getUser",                     // 要调用的方法名
                new Class<?>[] {String.class}, // 方法参数类型
                new Object[] {"user123"}       // 方法参数值
            );

            // 步骤 7: 通过反射读取和修改私有字段
            // 即使字段是 private 的，setAccessible(true) 后也可读写
            ReflectionExample.manipulateField(instance, "name", "UpdatedUserService");

        } catch (Exception e) {
            // 反射操作可能抛出多种异常：
            // - NoSuchMethodException：找不到方法
            // - NoSuchFieldException：找不到字段
            // - IllegalAccessException：无权访问（通常用 setAccessible 解决）
            // - InvocationTargetException：被调用的方法内部抛出了异常
            e.printStackTrace();
        }
    }

    /**
     * 演示 Lambda 表达式和函数式编程。
     *
     * <h2>什么是 Lambda 表达式？</h2>
     * <p>
     * Lambda 表达式是 Java 8 引入的一种简洁的代码书写方式，
     * 本质上是 <b>函数式接口</b>（只有一个抽象方法的接口）的匿名实现。
     * 语法格式为：{@code (参数) -> { 方法体 }}。
     * </p>
     *
     * <h2>什么是函数式编程？</h2>
     * <p>
     * 函数式编程是一种编程范式，强调：
     * </p>
     * <ul>
     *   <li><b>函数是一等公民：</b>函数可以作为参数传递、作为返回值返回</li>
     *   <li><b>不可变性：</b>避免修改已有数据，而是创建新数据</li>
     *   <li><b>声明式编程：</b>描述"做什么"而非"怎么做"</li>
     * </ul>
     *
     * <h2>本方法演示的函数式编程概念</h2>
     * <ol>
     *   <li>Lambda 表达式基础（Function、Consumer、Supplier、Predicate）</li>
     *   <li>方法引用（静态方法引用、实例方法引用）</li>
     *   <li>Stream API（过滤、映射、规约）</li>
     *   <li>自定义函数式接口</li>
     *   <li>高阶函数（接受函数作为参数或返回函数）</li>
     * </ol>
     */
    private static void demonstrateLambda() {
        System.out.println("\n\n╔═══════════════════════════════════════╗");
        System.out.println("║      Part 3: Lambda 与函数式编程演示   ║");
        System.out.println("╚═══════════════════════════════════════╝");

        // Lambda 表达式基础 — Java 8 内置的四个核心函数式接口
        LambdaExample.demonstrateLambda();

        // 方法引用 — 更简洁的 Lambda 写法
        LambdaExample.demonstrateMethodReference();

        // Stream API — 声明式集合处理
        LambdaExample.demonstrateStreamAPI();

        // 自定义函数式接口 — @FunctionalInterface 注解的作用
        LambdaExample.demonstrateCustomFunctionalInterface();

        // 高阶函数 — 将函数作为参数或返回值
        LambdaExample.demonstrateHigherOrderFunctions();
    }
}
