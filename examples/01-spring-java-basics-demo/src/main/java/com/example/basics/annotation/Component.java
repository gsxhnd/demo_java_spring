package com.example.basics.annotation;

import java.lang.annotation.*;

/**
 * 自定义类级别注解，用于标记一个类为"组件"。
 *
 * <h2>注解定义语法</h2>
 * <p>
 * 使用 {@code @interface} 关键字定义注解（不是 {@code interface}），
 * 这是 Java 为注解专门设计的语法。注解中的方法称为 <b>"注解元素"</b>（annotation element），
 * 形式上像抽象方法，但实际上表示注解的 <b>属性</b>。
 * </p>
 *
 * <h2>三个元注解详解</h2>
 * <p>
 * Java 提供了一系列 <b>元注解</b>（对注解进行注解的注解）来控制自定义注解的行为：
 * </p>
 *
 * <h3>{@code @Target(ElementType.TYPE)}</h3>
 * <p>
 * 指定这个注解可以应用在哪些 Java 元素上。它是一个编译期约束——
 * 如果你把注解用在错误的元素上，<b>编译器会报错</b>。
 * </p>
 * <p>
 * {@code ElementType.TYPE} 表示只能用于类（class）、接口（interface）、
 * 枚举（enum）和注解类型（annotation type）的声明上。
 * </p>
 * <p>
 * 其他常见值：{@code FIELD}（字段）、{@code METHOD}（方法）、
 * {@code PARAMETER}（参数）、{@code CONSTRUCTOR}（构造方法）、
 * {@code LOCAL_VARIABLE}（局部变量）、{@code ANNOTATION_TYPE}（注解类型）、
 * {@code PACKAGE}（包）等。
 * </p>
 *
 * <h3>{@code @Retention(RetentionPolicy.RUNTIME)}</h3>
 * <p>
 * 指定这个注解的 <b>生命周期</b>——注解信息保留到哪个阶段。
 * {@code RetentionPolicy} 有三个值：
 * </p>
 * <ul>
 *   <li>{@code SOURCE}：仅在源代码中存在，编译时丢弃。用于 IDE 提示、
 *       编译时检查（如 {@code @Override}）</li>
 *   <li>{@code CLASS}：编译到 .class 文件中，但 JVM 运行时不可见。是默认值。
 *       如果不需要运行时反射读取，建议用这个级别</li>
 *   <li>{@code RUNTIME}：编译到 .class 文件中，JVM 运行时可通过反射读取。
 *       <b>Spring 框架的注解几乎全部使用此级别</b>，因为需要运行时扫描和处理</li>
 * </ul>
 * <p>
 * 本项目中的 {@code @Component} 需要在运行时通过反射读取，因此使用 {@code RUNTIME}。
 * </p>
 *
 * <h3>{@code @Documented}</h3>
 * <p>
 * 指示在使用 {@code javadoc} 生成 API 文档时，应该将使用了该注解的类标记上这个注解信息。
 * 例如，如果一个类标注了 {@code @Component} 且使用了 {@code @Documented}，
 * 则在生成的 javadoc 中可以看到"该类带有 @Component 注解"的信息。
 * 这是一个纯文档层面的标记，不影响运行时行为。
 * </p>
 *
 * <h2>注解与 Spring 的关系</h2>
 * <p>
 * Spring 框架中有一个名为 {@code @Component} 的注解，功能非常类似——
 * 用于标记一个类为 Spring 管理的 Bean。Spring 在启动时会扫描所有带
 * {@code @Component}（以及 {@code @Service}、{@code @Repository}、{@code @Controller}）
 * 的类，并自动创建它们的实例放入 IoC 容器中。
 * </p>
 *
 * @see java.lang.annotation.Target
 * @see java.lang.annotation.Retention
 * @see java.lang.annotation.Documented
 */
@Target(ElementType.TYPE)       // 只能用在类/接口/枚举的声明上
@Retention(RetentionPolicy.RUNTIME) // 运行时可通过反射读取注解内容
@Documented                     // 生成 javadoc 时会显示此注解
public @interface Component {

    /**
     * 组件名称属性。
     *
     * <p>
     * 使用 {@code String value()} 定义注解属性，默认值是空字符串。
     * 属性名 {@code value} 是 Java 注解中的一个"特权名"——
     * 当注解只有一个名为 {@code value} 的属性时，使用时可以省略属性名，直接写值，
     * 例如：{@code @Component("myService")} 等价于 {@code @Component(value = "myService")}。
     * </p>
     *
     * <p>
     * 注解属性类型只能是：基本类型、String、Class、枚举、注解，
     * 以及这些类型的一维数组。不能使用自定义对象或集合类型。
     * </p>
     *
     * @return 组件名称，默认为空字符串
     */
    String value() default "";
}
