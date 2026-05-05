package com.example.basics.annotation;

import java.lang.annotation.*;

/**
 * 自定义字段级别注解，用于标记需要"注入"的字段。
 *
 * <h2>注解定义语法</h2>
 * <p>
 * 使用 {@code @interface} 关键字定义注解（不是 {@code interface}），
 * 这是 Java 为注解专门设计的语法。注解中的方法称为 <b>"注解元素"</b>，
 * 形式上像抽象方法，但实际上表示注解的 <b>属性</b>。
 * </p>
 *
 * <h2>三个元注解详解</h2>
 *
 * <h3>{@code @Target(ElementType.FIELD)}</h3>
 * <p>
 * 指定这个注解只能应用在 <b>字段声明</b> 上（包括枚举常量）。
 * 与 {@code @Component} 不同，它使用 {@code ElementType.FIELD} 而非 {@code TYPE}，
 * 表示它专门用于字段级别的标注。
 * </p>
 * <p>
 * 如果尝试将这个注解放在类或方法上，编译器会直接报错：
 * "annotation type not applicable to this kind of declaration"。
 * 这是 Java 编译器在编译期强制检查的约束。
 * </p>
 *
 * <h3>{@code @Retention(RetentionPolicy.RUNTIME)}</h3>
 * <p>
 * 注解信息在运行时仍然可以通过反射读取。
 * 这是必需的——因为我们的 {@code AnnotationProcessor} 需要在运行时
 * 扫描字段上的 {@code @Inject} 注解来决定如何处理依赖注入。
 * </p>
 * <p>
 * 如果改为 {@code SOURCE} 或 {@code CLASS}，则运行时无法读取，
 * 所有基于反射的注解处理逻辑都会失效。
 * </p>
 *
 * <h3>{@code @Documented}</h3>
 * <p>
 * 标记这个注解的信息应该出现在 javadoc API 文档中。
 * 这是一个纯文档层面的元注解，对运行时行为没有任何影响。
 * 如果去掉此注解，javadoc 生成器就不会在使用了 {@code @Inject} 的字段文档中
 * 标注该注解的存在。
 * </p>
 *
 * <h2>与 Spring 的 {@code @Autowired} 的对比</h2>
 * <p>
 * 这个 {@code @Inject} 注解模拟了 Spring 框架中的依赖注入注解（如 {@code @Autowired}、
 * JSR-330 的 {@code @Inject}）的核心思想：
 * </p>
 * <ul>
 *   <li>在字段上标注注解，表示"我需要被注入"</li>
 *   <li>框架通过反射扫描这些注解</li>
 *   <li>框架根据注解的值找到对应的依赖</li>
 *   <li>通过反射（{@code field.set()}）将依赖注入到字段中</li>
 * </ul>
 *
 * @see java.lang.annotation.Target
 * @see java.lang.annotation.Retention
 * @see java.lang.annotation.Documented
 */
@Target(ElementType.FIELD)      // 只能用在字段声明上（不能在方法或类上使用）
@Retention(RetentionPolicy.RUNTIME) // 运行时可通过反射读取注解内容
@Documented                     // 生成 javadoc 时会显示此注解
public @interface Inject {

    /**
     * 指定要注入的依赖 Bean 名称。
     *
     * <p>
     * 默认值为空字符串。在真实的 Spring 中，当不指定名称时，
     * Spring 会按"类型匹配"的方式自动装配；指定名称时，
     * 则按"名称匹配"的方式装配。
     * </p>
     *
     * <p>
     * 注解属性类型只能是：基本类型、String、Class、枚举、注解，
     * 以及这些类型的一维数组。这是 Java 注解的设计限制。
     * </p>
     *
     * @return 依赖的 Bean 名称，默认为空字符串（表示按类型匹配）
     */
    String value() default "";
}
