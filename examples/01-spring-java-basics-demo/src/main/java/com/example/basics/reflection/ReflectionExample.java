package com.example.basics.reflection;

import java.lang.reflect.*;

/**
 * Java 反射机制示例——演示如何在运行时检查和操作类的结构。
 *
 * <h2>什么是反射（Reflection）？</h2>
 * <p>
 * 反射是 Java 语言提供的一种强大机制，允许程序在 <b>运行时</b> 获取任意一个类的
 * 内部信息（类名、字段、方法、构造方法、注解等），并且可以动态创建对象、
 * 调用方法、读写字段——即使这些成员被声明为 {@code private}。
 * </p>
 *
 * <h2>反射的核心入口：{@code Class<?>}</h2>
 * <p>
 * {@code Class<?>} 对象是反射的起点。每一个被 JVM 加载的类都有一个对应的
 * {@code Class} 对象，其中包含了这个类的完整元数据。获取 Class 对象有三种方式：
 * </p>
 * <ol>
 *   <li>{@code 类名.class}：编译期确定，最常用</li>
 *   <li>{@code 对象.getClass()}：运行时从实例获取</li>
 *   <li>{@code Class.forName("全限定类名")}：动态加载，常用于框架</li>
 * </ol>
 *
 * <h2>反射的核心类</h2>
 * <table border="1">
 *   <tr><th>类</th><th>对应概念</th><th>获取方式</th></tr>
 *   <tr><td>{@code Class<?>}</td><td>类的元数据</td><td>{@code obj.getClass()} / {@code Class.forName()}</td></tr>
 *   <tr><td>{@code Constructor<?>}</td><td>构造方法</td><td>{@code clazz.getDeclaredConstructors()}</td></tr>
 *   <tr><td>{@code Field}</td><td>字段（成员变量）</td><td>{@code clazz.getDeclaredFields()}</td></tr>
 *   <tr><td>{@code Method}</td><td>方法</td><td>{@code clazz.getDeclaredMethods()}</td></tr>
 *   <tr><td>{@code AnnotatedElement}</td><td>可被注解的元素</td><td>Class/Field/Method 都实现此接口</td></tr>
 * </table>
 *
 * <h2>{@code getDeclaredXxx()} vs {@code getXxx()}</h2>
 * <table border="1">
 *   <tr><th>方法</th><th>返回范围</th><th>是否包括继承的</th></tr>
 *   <tr><td>{@code getDeclaredMethods()}</td><td>该类自身声明的所有方法（含 private）</td><td>否</td></tr>
 *   <tr><td>{@code getMethods()}</td><td>所有 public 方法</td><td>是（含父类/Object 的）</td></tr>
 *   <tr><td>{@code getDeclaredFields()}</td><td>该类自身声明的所有字段（含 private）</td><td>否</td></tr>
 *   <tr><td>{@code getFields()}</td><td>所有 public 字段</td><td>是（含父类的）</td></tr>
 *   <tr><td>{@code getDeclaredConstructors()}</td><td>该类自身声明的所有构造方法</td><td>否（构造方法不继承）</td></tr>
 * </table>
 *
 * <h2>反射与 Spring 框架</h2>
 * <p>
 * Spring 框架的 IoC 容器重度依赖反射。以下是 Spring 中用到反射的典型场景：
 * </p>
 * <ul>
 *   <li>通过反射扫描 {@code @Component} 注解，创建 Bean 实例</li>
 *   <li>通过反射调用 {@code @Bean} 方法获取 Bean</li>
 *   <li>通过 {@code field.setAccessible(true)} 突破 private，实现 {@code @Autowired} 字段注入</li>
 *   <li>通过反射调用 {@code @PostConstruct} / {@code @PreDestroy} 生命周期方法</li>
 *   <li>AOP 动态代理（JDK 动态代理和 CGLIB 都基于反射）</li>
 * </ul>
 *
 * <h2>反射的代价</h2>
 * <p>
 * 反射虽然强大，但也有一些代价：
 * </p>
 * <ul>
 *   <li><b>性能开销：</b>反射调用比直接调用慢，因为需要额外的类型检查和访问控制检查</li>
 *   <li><b>安全限制：</b>可能绕过类型安全（如在编译期不可见的错误要到运行时才暴露）</li>
 *   <li><b>代码可读性：</b>反射代码通常比直接调用更难理解</li>
 * </ul>
 */
public class ReflectionExample {

    /**
     * 打印类的元数据（Metadata）信息——类名、父类、接口。
     *
     * <h2>涉及的反射 API</h2>
     * <ul>
     *   <li>{@code clazz.getName()}：返回类的完整限定名（包名.类名）</li>
     *   <li>{@code clazz.getSimpleName()}：返回类的简短名称（仅类名，不含包名）</li>
     *   <li>{@code clazz.getSuperclass()}：返回父类的 Class 对象，
     *       如果该类是 {@code Object} 的直接子类则返回 {@code java.lang.Object}，
     *       如果是接口或基本类型则返回 {@code null}</li>
     *   <li>{@code clazz.getInterfaces()}：返回该类实现的所有接口的 Class 对象数组</li>
     * </ul>
     *
     * @param clazz 要检查的类的 Class 对象。泛型 {@code <?>} 表示可以接受任意类型的 Class
     */
    public static void printClassMetadata(Class<?> clazz) {
        System.out.println("\n===== 类元信息 =====");
        // getName()：获取完整类名（含包名），如 "com.example.basics.model.UserService"
        System.out.println("类名: " + clazz.getName());
        // getSimpleName()：获取简单类名（不含包名），如 "UserService"
        System.out.println("简单类名: " + clazz.getSimpleName());
        // getSuperclass()：获取直接父类，所有类默认继承 Object，所以通常不为 null
        System.out.println("父类: " + (clazz.getSuperclass() != null ? clazz.getSuperclass().getName() : "无"));

        // getInterfaces()：获取该类实现的所有接口（不包括父类实现的接口）
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
            System.out.print("实现的接口: ");
            for (Class<?> iface : interfaces) {
                System.out.print(iface.getName() + " ");
            }
            System.out.println();
        }
    }

    /**
     * 打印类的所有构造方法。
     *
     * <h2>涉及的反射 API</h2>
     * <ul>
     *   <li>{@code clazz.getDeclaredConstructors()}：返回 {@code Constructor<?>[]}，
     *       包含该类自身声明的所有构造方法（public、protected、默认、private），
     *       <b>不包括父类构造方法</b>（子类不继承构造方法）</li>
     *   <li>{@code constructor.getName()}：返回构造方法的名称（等于类名）</li>
     *   <li>{@code constructor.getParameterTypes()}：返回构造方法的参数类型数组</li>
     * </ul>
     *
     * @param clazz 要检查的类的 Class 对象
     */
    public static void printConstructors(Class<?> clazz) {
        System.out.println("\n===== 构造方法 =====");
        // getDeclaredConstructors()：获取该类声明的所有构造方法
        // 注意：构造方法不被继承，所以不需要区分 Declared 和普通版本
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            // 打印构造方法名 + 开括号
            System.out.print(constructor.getName() + "(");
            // getParameterTypes()：获取参数的 Class 对象数组
            Class<?>[] params = constructor.getParameterTypes();
            for (int i = 0; i < params.length; i++) {
                // getSimpleName() 获取参数的简短类名
                System.out.print(params[i].getSimpleName());
                // 不是最后一个参数时添加逗号分隔符
                if (i < params.length - 1) System.out.print(", ");
            }
            System.out.println(")");
        }
    }

    /**
     * 打印类的所有方法（包括私有方法）。
     *
     * <h2>涉及的反射 API</h2>
     * <ul>
     *   <li>{@code clazz.getDeclaredMethods()}：返回 {@code Method[]}，包含该类自身声明的
     *       所有方法（public/protected/默认/private），<b>不包括从父类继承的方法</b>。
     *       例如：UserService 里的方法会返回，但从 Object 继承的 toString/hashCode 不会返回。</li>
     *   <li>{@code method.getReturnType()}：返回方法返回值类型的 Class 对象</li>
     *   <li>{@code method.getName()}：返回方法名称</li>
     *   <li>{@code method.getParameterTypes()}：返回方法参数类型的 Class 数组</li>
     * </ul>
     *
     * <p>
     * 如果想要获取包括父类继承的 public 方法，应该使用 {@code getMethods()}。
     * 两者结果的差异是理解 Java 反射的一个重要知识点。
     * </p>
     *
     * @param clazz 要检查的类的 Class 对象
     */
    public static void printMethods(Class<?> clazz) {
        System.out.println("\n===== 方法 =====");
        // getDeclaredMethods()：只返回该类自身声明的所有方法
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            // 返回值类型 + 方法名 + 开括号
            // 例如打印 "String getUser(" 或 "void setName("
            System.out.print(method.getReturnType().getSimpleName() + " " +
                           method.getName() + "(");
            // getParameterTypes()：获取参数的 Class 对象数组
            Class<?>[] params = method.getParameterTypes();
            for (int i = 0; i < params.length; i++) {
                System.out.print(params[i].getSimpleName());
                if (i < params.length - 1) System.out.print(", ");
            }
            System.out.println(")");
        }
    }

    /**
     * 打印类的所有字段（包括私有字段）。
     *
     * <h2>涉及的反射 API</h2>
     * <ul>
     *   <li>{@code clazz.getDeclaredFields()}：返回 {@code Field[]}，包含该类自身声明的
     *       所有字段（public/protected/默认/private），<b>不包括从父类继承的字段</b>。</li>
     *   <li>{@code field.getType()}：返回字段类型的 Class 对象</li>
     *   <li>{@code field.getName()}：返回字段名称</li>
     * </ul>
     *
     * <p>
     * 对于 UserService 类，这个方法会输出：
     * {@code String repository} 和 {@code String name} 两个字段。
     * </p>
     *
     * @param clazz 要检查的类的 Class 对象
     */
    public static void printFields(Class<?> clazz) {
        System.out.println("\n===== 字段 =====");
        // getDeclaredFields()：只返回该类自身声明的所有字段
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            // 打印 "类型 字段名"，例如 "String repository" 或 "String name"
            System.out.println(field.getType().getSimpleName() + " " + field.getName());
        }
    }

    /**
     * 通过反射动态创建对象实例。
     *
     * <h2>技术步骤</h2>
     * <ol>
     *   <li>{@code clazz.getDeclaredConstructor(paramTypes)}：根据参数类型查找匹配的构造方法</li>
     *   <li>{@code constructor.setAccessible(true)}：<b>突破访问限制！</b>
     *       如果构造方法是 private 的，需要先调用此方法才能调用。</li>
     *   <li>{@code constructor.newInstance(args)}：传入实际参数值，创建对象实例</li>
     * </ol>
     *
     * <h2>{@code setAccessible(true)} 的作用</h2>
     * <p>
     * Java 的访问控制（private/protected/默认）在反射中仍然有效——即使通过反射获得了
     * private 成员，直接调用也会抛出 {@code IllegalAccessException}。
     * {@code setAccessible(true)} 是一种"暴力破解"手段，它告诉 JVM：
     * "忽略 Java 的访问控制检查"。这是很多框架（如 Spring）实现依赖注入的关键。
     * </p>
     *
     * @param clazz     要实例化的类的 Class 对象
     * @param paramTypes 构造方法的参数类型数组，如 {@code new Class[]{String.class}}
     * @param args       传递给构造方法的实际参数值数组，与 paramTypes 一一对应
     * @return 创建的对象实例
     * @throws NoSuchMethodException     如果找不到匹配的构造方法
     * @throws InstantiationException    如果类是抽象类或接口（无法实例化）
     * @throws IllegalAccessException    如果构造方法不可访问（需要先 setAccessible）
     * @throws InvocationTargetException 如果构造方法内部抛出了异常（原异常的包装）
     */
    public static Object instantiateClass(Class<?> clazz, Class<?>[] paramTypes, Object[] args)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        System.out.println("\n===== 动态实例化 =====");
        // 根据参数类型获取对应的构造方法对象
        Constructor<?> constructor = clazz.getDeclaredConstructor(paramTypes);
        // 突破访问控制：即使是 private 的构造方法也能调用
        constructor.setAccessible(true);
        // 调用构造方法创建实例，传入参数值
        Object instance = constructor.newInstance(args);
        System.out.println("成功创建 " + clazz.getSimpleName() + " 的实例");
        return instance;
    }

    /**
     * 通过反射动态调用方法。
     *
     * <h2>技术步骤</h2>
     * <ol>
     *   <li>{@code instance.getClass().getDeclaredMethod(methodName, paramTypes)}：
     *       根据方法名和参数类型查找匹配的方法对象。
     *       注意：方法名是纯字符串，必须精确匹配（大小写敏感）</li>
     *   <li>{@code method.setAccessible(true)}：突破访问限制，即使是 private 方法也能调用</li>
     *   <li>{@code method.invoke(instance, args)}：调用该方法
     *       <ul>
     *         <li>第一个参数是目标对象实例（如果是静态方法则传 null）</li>
     *         <li>后续参数是方法的实际参数值</li>
     *         <li>返回类型是 Object（原始类型会自动装箱）</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * <h2>invoke() 方法详解</h2>
     * <p>
     * {@code method.invoke(obj, args...)} 是反射方法调用的核心。它的工作机制：
     * </p>
     * <ol>
     *   <li>JVM 找到该方法在类中的位置</li>
     *   <li>进行访问权限检查（除非 setAccessible(true)）</li>
     *   <li>将参数与方法的形参列表进行类型匹配</li>
     *   <li>调用目标的字节码指令</li>
     *   <li>将返回值（如果是 void 则返回 null）包装为 Object</li>
     * </ol>
     *
     * @param instance   要调用方法的目标对象实例
     * @param methodName 要调用的方法名称（字符串，大小写敏感）
     * @param paramTypes 方法的参数类型数组，用于精确定位重载方法
     * @param args       传给方法的实际参数值数组
     * @return 方法的返回值（Object 类型，原始类型会自动装箱）
     * @throws NoSuchMethodException     如果找不到匹配的方法
     * @throws IllegalAccessException    如果方法不可访问
     * @throws InvocationTargetException 如果方法内部抛出了异常
     */
    public static Object invokeMethod(Object instance, String methodName,
                                     Class<?>[] paramTypes, Object[] args)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        System.out.println("\n===== 动态方法调用 =====");
        // getDeclaredMethod() 根据方法名和参数类型获取方法对象
        // 注意：方法名必须精确匹配（大小写敏感），参数类型也必须是精确匹配的
        Method method = instance.getClass().getDeclaredMethod(methodName, paramTypes);
        // 突破访问限制：即使是 private 方法也能反射调用
        method.setAccessible(true);
        // invoke() 执行方法调用
        // 第一个参数是目标对象实例（如果是 static 方法则传 null）
        // 后续参数是方法的实际参数值
        Object result = method.invoke(instance, args);
        System.out.println("调用方法 " + methodName + "，返回值: " + result);
        return result;
    }

    /**
     * 通过反射动态读取和修改对象的字段值——即使是 private 字段。
     *
     * <h2>技术步骤</h2>
     * <ol>
     *   <li>{@code instance.getClass().getDeclaredField(fieldName)}：
     *       根据字段名称查找字段对象（字符串精确匹配）</li>
     *   <li>{@code field.setAccessible(true)}：突破 private 限制！！！</li>
     *   <li>{@code field.get(instance)}：读取字段的当前值</li>
     *   <li>{@code field.set(instance, newValue)}：将字段设置为新值</li>
     * </ol>
     *
     * <h2>Spring 依赖注入的核心原理</h2>
     * <p>
     * Spring 中 {@code @Autowired} 字段注入的底层实现就是通过反射的
     * {@code Field.set()} 实现的。简化流程如下：
     * </p>
     * <ol>
     *   <li>扫描 Bean 类的所有字段，找到标注了 {@code @Autowired} 的字段</li>
     *   <li>从 IoC 容器中找到匹配类型的 Bean</li>
     *   <li>调用 {@code field.setAccessible(true)} 突破 private 访问</li>
     *   <li>调用 {@code field.set(bean, dependency)} 将依赖注入</li>
     * </ol>
     * <p>
     * 这就是为什么你无需提供 setter 方法，Spring 也能把依赖注入到 private 字段中。
     * </p>
     *
     * @param instance  目标对象实例
     * @param fieldName 要操作的字段名称（字符串，大小写敏感）
     * @param newValue  要设置的新值
     * @throws NoSuchFieldException   如果找不到该字段
     * @throws IllegalAccessException 如果字段不可访问（需要先 setAccessible）
     */
    public static void manipulateField(Object instance, String fieldName, Object newValue)
            throws NoSuchFieldException, IllegalAccessException {
        System.out.println("\n===== 字段操作 =====");
        // getDeclaredField() 根据字段名查找字段对象
        // 注意：字段名必须精确匹配（包括大小写）
        Field field = instance.getClass().getDeclaredField(fieldName);
        // setAccessible(true) 突破 private 访问限制——反射中的"万能钥匙"
        field.setAccessible(true);
        // field.get(instance) 读取实例中该字段的当前值
        Object oldValue = field.get(instance);
        System.out.println("旧值: " + oldValue);
        // field.set(instance, newValue) 将字段值修改为新值
        field.set(instance, newValue);
        // 再次读取以验证修改是否生效
        System.out.println("新值: " + field.get(instance));
    }
}
