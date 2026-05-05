package com.example.basics.annotation;

import java.lang.annotation.*;

/**
 * 自定义方法级别注解，用于标记需要特殊处理的方法。
 *
 * <h2>注解定义语法</h2>
 * <p>
 * 使用 {@code @interface} 关键字定义注解。注解中的每个方法就是一个
 * <b>注解属性</b>（annotation element），如本注解中的 {@code handler()} 和
 * {@code validate()}。
 * </p>
 *
 * <h2>三个元注解详解</h2>
 *
 * <h3>{@code @Target(ElementType.METHOD)}</h3>
 * <p>
 * 指定这个注解只能应用在 <b>方法声明</b> 上。
 * 如果试图将 {@code @Process} 放在类或字段上，编译器会报错。
 * </p>
 * <p>
 * 注解的这种位置限制能力非常有用：在 Spring 框架中，你可以看到
 * {@code @PostConstruct} 只能用在方法上，{@code @Bean} 也只能用在方法上，
 * 都是通过 {@code @Target(ElementType.METHOD)} 来实现的。
 * </p>
 *
 * <h3>{@code @Retention(RetentionPolicy.RUNTIME)}</h3>
 * <p>
 * 注解信息保留到 JVM 运行时阶段。这是必需的，因为我们的
 * {@code AnnotationProcessor.processMethodAnnotation()} 需要在运行时
 * 通过反射读取方法上的 {@code @Process} 注解来获取 handler 和 validate 信息。
 * </p>
 * <p>
 * 如果没有 {@code RUNTIME} 级别的 Retention，运行时反射将无法读取到
 * 这个注解的任何属性值，注解处理器也就无法工作。
 * </p>
 *
 * <h3>{@code @Documented}</h3>
 * <p>
 * 让 javadoc 文档生成器在 API 文档中显示该注解的使用信息。
 * 例如，如果 {@code UserService.getUser()} 使用了 {@code @Process}，
 * 且注解带有 {@code @Documented}，则在生成的 javadoc 中可以看到
 * "{@code @Process(handler="getUserHandler", validate=true)}" 的描述。
 * </p>
 *
 * <h2>与 AOP 切面编程的关系</h2>
 * <p>
 * 方法级别的注解是实现 <b>AOP（面向切面编程）</b> 的关键机制。
 * 在 Spring 中：
 * </p>
 * <ul>
 *   <li>{@code @Transactional} 标记方法需要事务管理</li>
 *   <li>{@code @Cacheable} 标记方法结果需要缓存</li>
 *   <li>{@code @Async} 标记方法需要异步执行</li>
 *   <li>{@code @Scheduled} 标记方法需要定时执行</li>
 * </ul>
 * <p>
 * 这些注解本身没有逻辑，都是通过 AOP 代理在方法调用前后插入增强逻辑。
 * </p>
 *
 * <h2>多属性注解的特点</h2>
 * <p>
 * 这是一个包含 <b>两个属性</b> 的注解（{@code handler} 和 {@code validate}），
 * 展示了含有多个属性的注解如何定义和使用。使用时必须指定属性名：
 * {@code @Process(handler = "myHandler", validate = true)}。
 * 也展示了 {@code boolean} 类型属性带默认值的用法。
 * </p>
 *
 * @see java.lang.annotation.Target
 * @see java.lang.annotation.Retention
 * @see java.lang.annotation.Documented
 */
@Target(ElementType.METHOD)     // 只能用在方法声明上
@Retention(RetentionPolicy.RUNTIME) // 运行时可通过反射读取注解内容
@Documented                     // 生成 javadoc 时会显示此注解
public @interface Process {

    /**
     * 处理器名称，用于指定处理该方法的处理器标识。
     *
     * <p>
     * 默认值为 {@code "default"}。在复杂的系统中，不同的方法可能需要
     * 不同的处理器来处理，这个属性用于区分不同的处理策略。
     * </p>
     *
     * @return 处理器名称，默认为 "default"
     */
    String handler() default "default";

    /**
     * 是否需要对参数进行验证。
     *
     * <p>
     * 这是一个 {@code boolean} 类型的注解属性，默认值为 {@code true}。
     * 展示了注解中 boolean 属性的用法——可以设为 true/false 来控制行为。
     * </p>
     *
     * @return 是否需要验证，默认为 true
     */
    boolean validate() default true;
}
