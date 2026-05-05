package com.example.basics.model;

import com.example.basics.annotation.Component;
import com.example.basics.annotation.Inject;
import com.example.basics.annotation.Process;

/**
 * 用户服务类——作为被注解标记的示例类，演示自定义注解在类、字段、方法上的使用。
 *
 * <h2>这个类展示了三种级别的注解</h2>
 * <ul>
 *   <li><b>类级别：</b>{@code @Component(value = "userService")} —
 *       标记这个类是一个"组件"，类似于 Spring 中的 {@code @Component} 或 {@code @Service}</li>
 *   <li><b>字段级别：</b>{@code @Inject("userRepository")} —
 *       标记 {@code repository} 字段需要依赖注入，类似于 Spring 中的 {@code @Autowired}</li>
 *   <li><b>方法级别：</b>{@code @Process(handler = "...")} —
 *       标记方法需要特殊处理，类似于 Spring AOP 中的切点定义</li>
 * </ul>
 *
 * <h2>与 Spring 框架中的对应关系</h2>
 * <table border="1">
 *   <tr><th>本项目的注解</th><th>Spring 框架中的对应</th><th>用途</th></tr>
 *   <tr><td>{@code @Component}</td><td>{@code @Component / @Service / @Repository / @Controller}</td><td>标记为 Spring Bean</td></tr>
 *   <tr><td>{@code @Inject}</td><td>{@code @Autowired / @Inject(JSR-330) / @Resource(JSR-250)}</td><td>依赖注入</td></tr>
 *   <tr><td>{@code @Process}</td><td>{@code @Transactional / @Cacheable / @Async}</td><td>AOP 切面拦截</td></tr>
 * </table>
 *
 * <h2>注解处理流程（以 Spring 为例）</h2>
 * <ol>
 *   <li>Spring 容器启动，扫描指定包下的所有 .class 文件</li>
 *   <li>发现 {@code @Component} 标注的类，将其注册为 Bean 定义</li>
 *   <li>扫描 Bean 类中的 {@code @Autowired/@Inject} 字段</li>
 *   <li>通过反射（{@code Field.set()}）将依赖注入到字段中</li>
 *   <li>扫描 Bean 类中的 {@code @PostConstruct} 等方法并调用</li>
 *   <li>对于带有 {@code @Transactional} 等方法，创建 AOP 代理包装方法调用</li>
 * </ol>
 */
@Component(value = "userService")  // 类级别注解：标记此类为"组件"，名称为 "userService"
public class UserService {

    // 字段级别注解：标记此字段需要注入名为 "userRepository" 的 Bean
    // 在真实 Spring 应用中，@Autowired 会让 Spring 自动查找匹配类型的 Bean 并赋值
    @Inject("userRepository")
    private String repository;

    // 普通字段：不使用任何注解，用于演示反射中的字段读写操作
    private String name;

    /**
     * 无参构造方法。
     *
     * <p>
     * 当类没有定义任何构造方法时，Java 编译器会自动生成一个无参构造方法。
     * 但由于本类还定义了有参构造方法（见下方），因此必须显式定义此无参构造方法。
     * </p>
     */
    public UserService() {
        this.name = "UserService";
    }

    /**
     * 有参构造方法。
     *
     * <p>
     * 用于演示通过反射调用有参构造方法动态创建对象实例。
     * 在反射示例中，{@code instantiateClass()} 方法就是通过此构造方法创建实例的。
     * </p>
     *
     * @param name 服务名称
     */
    public UserService(String name) {
        this.name = name;
    }

    /**
     * 获取用户信息的方法——标注了 {@code @Process} 注解。
     *
     * <p>
     * {@code @Process(handler = "getUserHandler", validate = true)} 的含义：
     * </p>
     * <ul>
     *   <li>{@code handler = "getUserHandler"}：指定处理器标识，
     *       在真实系统中可由此路由到具体的处理器逻辑</li>
     *   <li>{@code validate = true}：表示在处理前需要参数验证，
     *       框架可以先校验 userId 是否合法再执行方法</li>
     * </ul>
     *
     * @param userId 用户 ID
     * @return 带有用户 ID 信息的字符串
     */
    @Process(handler = "getUserHandler", validate = true)  // 方法级别注解：标记此方法需要特殊处理
    public String getUser(String userId) {
        return "User: " + userId;
    }

    /**
     * 保存用户信息的方法——同样标注了 {@code @Process} 注解，但使用了不同的 handler。
     *
     * <p>
     * {@code handler = "saveUserHandler"} 与上面的 {@code getUserHandler} 不同，
     * 表示不同的业务操作可能由不同的处理器来处理。展示了一类多注解场景。
     * </p>
     *
     * @param userId 用户 ID
     * @param userData 用户数据
     * @return 保存是否成功
     */
    @Process(handler = "saveUserHandler", validate = true)  // 不同的 handler 表示不同的处理策略
    public boolean saveUser(String userId, String userData) {
        return true;
    }

    /**
     * 更新用户——没有标注 {@code @Process} 注解的普通方法。
     *
     * <p>
     * 用于对比：有注解和无注解的方法在扫描时会被不同对待。
     * 注解处理器（{@code AnnotationProcessor}）在处理时只会识别
     * 带有 {@code @Process} 注解的方法，像这个方法就不会被识别到。
     * </p>
     *
     * @param userId 用户 ID
     * @param data 更新数据
     */
    public void updateUser(String userId, String data) {
        System.out.println("更新用户: " + userId);
    }

    /**
     * 获取服务名称（getter 方法）。
     *
     * <p>
     * 标准的 Java Bean getter 方法，与同名的 {@code name} 字段配对。
     * 遵循 Java Bean 命名规范：{@code get + 首字母大写的字段名}。
     * 在 Spring、MyBatis 等框架中，属性名通常由此推断出来（去掉 get/is 前缀并首字母小写）。
     * </p>
     *
     * @return 服务名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置服务名称（setter 方法）。
     *
     * <p>
     * 标准的 Java Bean setter 方法，通常与 getter 配对使用。
     * </p>
     *
     * @param name 新的服务名称
     */
    public void setName(String name) {
        this.name = name;
    }
}
