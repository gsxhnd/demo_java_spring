package com.example.basics.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 注解处理器，负责在运行时通过反射机制读取并处理自定义注解。
 *
 * <h2>核心概念：注解处理流程</h2>
 * <p>
 * 注解本身只是"标签"，不会自动执行任何逻辑。必须有一个 <b>注解处理器</b>
 * 在运行时（或编译时）读取注解，并根据注解的属性值执行相应的处理逻辑。
 * 这个处理流程被称为 <b>注解扫描</b>（Annotation Scanning）。
 * </p>
 *
 * <h2>Spring 框架中的注解处理</h2>
 * <p>
 * Spring 的 {@code ApplicationContext} 启动时，{@code ComponentScan} 机制
 * 会做以下事情（与本类的处理方式类似但更复杂）：
 * </p>
 * <ol>
 *   <li>扫描指定包下所有的 {@code .class} 文件</li>
 *   <li>检查每个类是否带有 {@code @Component} 注解</li>
 *   <li>对于符合条件的类，通过反射创建实例（Bean）</li>
 *   <li>检查字段上的 {@code @Autowired} 等注解</li>
 *   <li>通过反射将依赖注入到字段中（{@code field.set(bean, dependency)}）</li>
 *   <li>检查方法上的 {@code @PostConstruct} 等注解并调用</li>
 * </ol>
 *
 * <h2>反射关键方法</h2>
 * <ul>
 *   <li>{@code Class.getDeclaredMethods()}：获取该类自身声明的所有方法（不包括继承的）</li>
 *   <li>{@code Class.getDeclaredFields()}：获取该类自身声明的所有字段（不包括继承的）</li>
 *   <li>{@code AnnotatedElement.isAnnotationPresent(Class)}：检查元素上是否存在指定注解</li>
 *   <li>{@code AnnotatedElement.getAnnotation(Class)}：获取元素上指定注解的实例（包含属性值）</li>
 * </ul>
 * <p>
 * 注意：{@code getDeclaredXxx} 系列方法返回该类自身声明的成员（包括 private 的），
 * 但不包括从父类继承的；而 {@code getXxx} 系列方法只返回 public 成员（包括继承的）。
 * </p>
 */
public class AnnotationProcessor {

    /**
     * 处理类级别的 {@code @Component} 注解。
     *
     * <h2>技术步骤</h2>
     * <ol>
     *   <li>检查类是否标注了 {@code @Component} 注解：
     *       {@code clazz.isAnnotationPresent(Component.class)}</li>
     *   <li>获取注解实例以读取属性值：
     *       {@code clazz.getAnnotation(Component.class)}</li>
     *   <li>读取注解的 {@code value()} 属性获取组件名称</li>
     * </ol>
     *
     * <h2>关键方法说明</h2>
     * <ul>
     *   <li><b>{@code isAnnotationPresent()}：</b>检查当前元素上是否存在指定注解。
     *       这是一个"存在性检查"，类似于 {@code instanceof}。
     *       由于 Java 注解使用了动态代理，这个方法内部是通过
     *       {@code AnnotatedElement.getAnnotation()} 是否返回非 null 来判断的。</li>
     *   <li><b>{@code getAnnotation()}：</b>获取注解的"实例"。
     *       由于注解本质是接口，JVM 在运行时会通过 <b>动态代理</b>
     *       生成一个实现了该注解接口的代理对象，让开发者可以通过调用方法
     *       （如 {@code component.value()}）来访问注解的属性值。</li>
     * </ul>
     *
     * <h2>为什么传入的是 Class 对象？</h2>
     * <p>
     * 因为这个方法只处理"类的元数据"层面的注解，不需要实际的类实例。
     * 注解是放在 {@code .class} 文件中的元数据，读取注解只需要 Class 对象即可。
     * </p>
     *
     * @param clazz 要检查的类的 Class 对象。{@code Class<?>} 中的通配符 {@code ?}
     *              表示可以接受任何类型的 Class 对象
     */
    public static void processComponentAnnotation(Class<?> clazz) {
        // isAnnotationPresent() 运行时检查类是否标注了 @Component 注解
        if (clazz.isAnnotationPresent(Component.class)) {
            // getAnnotation() 获取注解的代理实例，可以像调用普通方法一样获取属性值
            Component component = clazz.getAnnotation(Component.class);
            // 通过注解实例读取 value 属性（即组件名称）
            String name = component.value();
            // 如果名称为空则只打印类名，否则同时打印名称信息
            System.out.println("[Component] 发现组件: " + clazz.getSimpleName() +
                               (name.isEmpty() ? "" : " (名称: " + name + ")"));
        }
    }

    /**
     * 处理方法级别的 {@code @Process} 注解。
     *
     * <h2>技术步骤</h2>
     * <ol>
     *   <li>通过反射获取该实例对应类的所有方法：
     *       {@code instance.getClass().getDeclaredMethods()}</li>
     *   <li>遍历每个方法，检查是否标注了 {@code @Process}</li>
     *   <li>读取注解的 {@code handler()} 和 {@code validate()} 属性</li>
     * </ol>
     *
     * <h2>关键方法说明</h2>
     * <ul>
     *   <li><b>{@code getDeclaredMethods()}：</b>返回 {@code Method[]} 数组，
     *       包含该类自身声明的所有方法（public、protected、默认、private 都包括），
     *       但不包括从父类继承的方法。与此相对，{@code getMethods()} 只返回
     *       public 方法（包括从父类继承的 public 方法）。</li>
     * </ul>
     *
     * <h2>为什么这里传入的是"实例对象"？</h2>
     * <p>
     * 与 {@code processComponentAnnotation} 不同，这里传入的是实例对象。
     * 这是因为获取方法信息同样只需要 Class 对象（通过 {@code instance.getClass()}），
     * 但实际开发中更常见的场景是：拿到一个已有对象后，扫描其方法上的注解并做处理。
     * 例如 Spring 在处理 {@code @Bean} 方法时，就是通过已经创建好的
     * {@code @Configuration} 实例来查找方法上的 {@code @Bean} 注解。
     * </p>
     *
     * @param instance 要检查的对象实例。之所以传入实例而非 Class，是为了模拟
     *                 真实场景中"拿到已有对象后扫描其方法注解"的情况
     */
    public static void processMethodAnnotation(Object instance) {
        // getDeclaredMethods() 获取该类自身声明的所有方法（不包括父类继承的）
        Method[] methods = instance.getClass().getDeclaredMethods();
        for (Method method : methods) {
            // 检查方法上是否标注了 @Process 注解
            if (method.isAnnotationPresent(Process.class)) {
                // getAnnotation() 获取方法上的 @Process 注解实例（JVM 通过动态代理生成）
                Process processAnnotation = method.getAnnotation(Process.class);
                // 读取注解中的 handler 和 validate 属性值
                System.out.println("[Process] 发现处理方法: " + method.getName() +
                                   " (handler=" + processAnnotation.handler() +
                                   ", validate=" + processAnnotation.validate() + ")");
            }
        }
    }

    /**
     * 处理字段级别的 {@code @Inject} 注解。
     *
     * <h2>技术步骤</h2>
     * <ol>
     *   <li>通过反射获取该类的所有字段：
     *       {@code clazz.getDeclaredFields()}</li>
     *   <li>遍历每个字段，检查是否标注了 {@code @Inject}</li>
     *   <li>读取注解的 {@code value()} 属性获取依赖 Bean 名称</li>
     * </ol>
     *
     * <h2>关键方法说明</h2>
     * <ul>
     *   <li><b>{@code getDeclaredFields()}：</b>返回 {@code Field[]} 数组，
     *       包含该类自身声明的所有字段（public、protected、默认、private 都包括），
     *       但不包括从父类继承的字段。与此相对，{@code getFields()} 只返回
     *       public 字段（包括从父类继承的 public 字段）。</li>
     * </ul>
     *
     * <h2>与 Spring 的依赖注入对比</h2>
     * <p>
     * 本方法目前只做"扫描并打印"，不会真正注入依赖。真正的依赖注入需要：
     * </p>
     * <ol>
     *   <li>找到对应的依赖 Bean 实例（从 IoC 容器中）</li>
     *   <li>调用 {@code field.setAccessible(true)} 突破 private 限制</li>
     *   <li>调用 {@code field.set(instance, dependency)} 将依赖赋值给字段</li>
     * </ol>
     * <p>
     * Spring 的 {@code AutowiredAnnotationBeanPostProcessor} 实现原理与此类似。
     * </p>
     *
     * @param clazz 要检查的类的 Class 对象。字段注解是放在类定义中的，
     *              不需要实例即可读取
     */
    public static void processFieldAnnotation(Class<?> clazz) {
        // getDeclaredFields() 获取该类自身声明的所有字段（包括 private 的）
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            // 检查字段上是否标注了 @Inject 注解
            if (field.isAnnotationPresent(Inject.class)) {
                // getAnnotation() 获取字段上的 @Inject 注解实例
                Inject inject = field.getAnnotation(Inject.class);
                // 读取注解的 value 属性（要注入的 Bean 名称）
                System.out.println("[Inject] 发现注入字段: " + field.getName() +
                                   " (bean=" + inject.value() + ")");
            }
        }
    }
}
