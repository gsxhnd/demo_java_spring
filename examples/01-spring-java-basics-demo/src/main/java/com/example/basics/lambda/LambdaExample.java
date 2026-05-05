package com.example.basics.lambda;

import java.util.Arrays;
import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Lambda 表达式和函数式编程示例。
 *
 * <h2>为什么要学 Lambda？</h2>
 * <p>
 * Java 8 之前，要实现一个简单接口（如 Runnable、Comparator），必须写匿名内部类，
 * 代码冗长且可读性差。Lambda 表达式提供了更简洁的语法，让 Java 也能使用
 * 函数式编程风格来编写代码。
 * </p>
 *
 * <h2>Lambda 语法</h2>
 * <pre>{@code
 * (参数列表) -> { 方法体 }
 * (参数列表) -> 表达式  // 如果方法体是单条语句，可省略 {} 和 return
 * }</pre>
 *
 * <h2>四大核心函数式接口（java.util.function 包）</h2>
 * <table border="1">
 *   <tr><th>接口</th><th>抽象方法</th><th>输入</th><th>输出</th><th>用途</th></tr>
 *   <tr><td>{@code Function<T, R>}</td><td>{@code R apply(T t)}</td><td>T</td><td>R</td><td>转换/映射</td></tr>
 *   <tr><td>{@code Consumer<T>}</td><td>{@code void accept(T t)}</td><td>T</td><td>无</td><td>消费/打印</td></tr>
 *   <tr><td>{@code Supplier<T>}</td><td>{@code T get()}</td><td>无</td><td>T</td><td>提供/工厂</td></tr>
 *   <tr><td>{@code Predicate<T>}</td><td>{@code boolean test(T t)}</td><td>T</td><td>boolean</td><td>判断/过滤</td></tr>
 * </table>
 *
 * <h2>本类包含的演示内容</h2>
 * <ol>
 *   <li>Lambda 表达式基础（四大函数式接口的使用）</li>
 *   <li>方法引用（静态方法引用、实例方法引用）</li>
 *   <li>Stream API（filter、map、reduce 操作）</li>
 *   <li>自定义函数式接口</li>
 *   <li>高阶函数（函数作为参数和返回值）</li>
 * </ol>
 */
public class LambdaExample {

    /**
     * 演示 Java 8 四大核心函数式接口的基本 Lambda 用法。
     *
     * <h2>{@code Function<T, R>}</h2>
     * <p>
     * 函数型接口，接收一个类型为 T 的参数，返回一个类型为 R 的结果。
     * 抽象方法：{@code R apply(T t)}。
     * 经典用途：数据转换（如 {@code Stream.map()} 的参数）。
     * 即：<b>有输入，有输出</b>。
     * </p>
     *
     * <h2>{@code Consumer<T>}</h2>
     * <p>
     * 消费型接口，接收一个类型为 T 的参数，不返回任何结果。
     * 抽象方法：{@code void accept(T t)}。
     * 经典用途：遍历集合（如 {@code Iterable.forEach()} 的参数）。
     * 即：<b>有输入，无输出</b>（"消费"数据）。
     * </p>
     *
     * <h2>{@code Supplier<T>}</h2>
     * <p>
     * 供给型接口，不接受任何参数，返回一个类型为 T 的结果。
     * 抽象方法：{@code T get()}。
     * 经典用途：延迟计算、工厂方法（如 {@code Optional.orElseGet()} 的参数）。
     * 即：<b>无输入，有输出</b>（"提供"数据）。
     * </p>
     *
     * <h2>{@code Predicate<T>}</h2>
     * <p>
     * 断言型接口，接收一个类型为 T 的参数，返回 {@code boolean}。
     * 抽象方法：{@code boolean test(T t)}。
     * 经典用途：条件过滤（如 {@code Stream.filter()} 的参数）。
     * 即：<b>有输入，输出 boolean</b>（"判断"真伪）。
     * </p>
     */
    public static void demonstrateLambda() {
        System.out.println("\n===== Lambda 表达式 =====");

        // Function<T, R>：接收一个参数，返回一个结果
        // 泛型 Function<Integer, Integer> 表示输入和输出都是 Integer 类型
        // 语法 x -> x * x 是 "参数 -> 表达式" 形式，编译器会自动推断参数类型
        Function<Integer, Integer> square = x -> x * x;
        // apply() 方法是 Function 接口的唯一抽象方法
        System.out.println("5 的平方: " + square.apply(5));

        // Consumer<T>：接收一个参数，无返回值（"消费"这个参数）
        // Lambda 体中有多条语句时需要用 {} 包裹
        Consumer<String> printer = text -> System.out.println("输出: " + text);
        // accept() 方法是 Consumer 接口的唯一抽象方法
        printer.accept("Hello Lambda");

        // Supplier<T>：无参数，返回一个值（"供应"数据）
        // () -> "..." 表示无参数 Lambda 表达式
        Supplier<String> supplier = () -> "这是供应商的结果";
        // get() 方法是 Supplier 接口的唯一抽象方法
        System.out.println("Supplier: " + supplier.get());

        // Predicate<T>：接收一个参数，返回 boolean（"断言"真伪）
        // 条件判断 Lambda，返回类型由编译器自动推断为 boolean
        Predicate<Integer> isPositive = n -> n > 0;
        // test() 方法是 Predicate 接口的唯一抽象方法
        System.out.println("5 是正数吗? " + isPositive.test(5));
    }

    /**
     * 演示方法引用（Method Reference）——一种更简洁的 Lambda 写法。
     *
     * <h2>什么是方法引用？</h2>
     * <p>
     * 当 Lambda 表达式仅仅是调用一个已有的方法时，可以使用方法引用来简化代码。
     * 方法引用使用 {@code ::} 双冒号操作符。
     * </p>
     *
     * <h2>四种方法引用类型</h2>
     * <table border="1">
     *   <tr><th>类型</th><th>语法</th><th>等价的 Lambda</th></tr>
     *   <tr><td>静态方法引用</td><td>{@code ClassName::staticMethod}</td><td>{@code (args) -> ClassName.staticMethod(args)}</td></tr>
     *   <tr><td>特定对象的实例方法引用</td><td>{@code instance::method}</td><td>{@code (args) -> instance.method(args)}</td></tr>
     *   <tr><td>特定类的任意对象实例方法引用</td><td>{@code ClassName::instanceMethod}</td><td>{@code (obj, args) -> obj.instanceMethod(args)}</td></tr>
     *   <tr><td>构造方法引用</td><td>{@code ClassName::new}</td><td>{@code (args) -> new ClassName(args)}</td></tr>
     * </table>
     *
     * <p>
     * 本方法演示了：
     * </p>
     * <ul>
     *   <li>{@code System.out::println}：特定对象的实例方法引用</li>
     *   <li>{@code String::toUpperCase}：特定类的任意对象实例方法引用（通过 Stream 调用）</li>
     * </ul>
     */
    public static void demonstrateMethodReference() {
        System.out.println("\n===== 方法引用 =====");

        List<String> words = Arrays.asList("Hello", "World", "Lambda");

        // 实例方法引用：System.out::println
        // System.out 是一个 PrintStream 实例，println 是其实例方法
        // 等价于 Lambda：word -> System.out.println(word)
        System.out.print("大写: ");
        words.forEach(System.out::println);

        // 特定类的任意对象实例方法引用：String::toUpperCase
        // 注意：这里不是静态方法引用！toUpperCase 是 String 的实例方法
        // 这种写法的含义是：对每个字符串元素调用其自身的 toUpperCase() 方法
        // 等价于 Lambda：word -> word.toUpperCase()
        Consumer<String> printUpperCase = word -> System.out.println(word.toUpperCase());
        System.out.print("实例方法引用（大写）: ");
        // stream().map(String::toUpperCase) 将每个元素转换为大写
        words.stream().map(String::toUpperCase).forEach(System.out::println);
    }

    /**
     * 演示 Stream API 与 Lambda 表达式配合使用。
     *
     * <h2>什么是 Stream？</h2>
     * <p>
     * Stream 是 Java 8 引入的用于处理集合数据的 <b>声明式 API</b>。
     * 它不是数据结构，也不存储数据——它只是对数据源（集合、数组、I/O）的
     * 一种"数据流视图"，支持对数据进行过滤、转换、聚合等操作。
     * </p>
     *
     * <h2>Stream 操作分类</h2>
     * <ul>
     *   <li><b>中间操作（Intermediate）：</b>返回一个新的 Stream，可以链式调用。
     *       如：{@code filter()}、{@code map()}、{@code sorted()}。
     *       它们是 <b>惰性执行</b> 的——只有遇到终端操作时才会真正执行。</li>
     *   <li><b>终端操作（Terminal）：</b>返回最终结果或副作用，结束 Stream 管道。
     *       如：{@code collect()}、{@code forEach()}、{@code reduce()}、{@code count()}。</li>
     * </ul>
     *
     * <h2>{@code filter()} 详解</h2>
     * <p>
     * {@code filter(Predicate<T>)} 是中间操作，接收一个 Predicate（断言函数），
     * 保留满足条件的元素，过滤掉不满足的元素。
     * 本示例中 {@code n -> n % 2 == 0} 表示只保留偶数。
     * </p>
     *
     * <h2>{@code map()} 详解</h2>
     * <p>
     * {@code map(Function<T, R>)} 是中间操作，接收一个 Function（转换函数），
     * 将每个元素从类型 T 转换为类型 R，生成一个新的 Stream。
     * 本示例中 {@code n -> n * n} 将每个数字转换为其平方值。
     * </p>
     *
     * <h2>{@code reduce()} 详解</h2>
     * <p>
     * {@code reduce(identity, accumulator)} 是终端操作，用于将 Stream 中的所有元素
     * 按照指定的二元操作累积（归纳）成一个值。
     * </p>
     * <p>
     * 参数说明：
     * </p>
     * <ul>
     *   <li>{@code identity = 0}：初始值（identity value），即累加的起点</li>
     *   <li>{@code accumulator = Integer::sum}：累加器函数，
     *       形式为 {@code (a, b) -> a + b}，表示如何将当前结果与下一个元素合并</li>
     * </ul>
     * <p>
     * 执行过程：初始值 0，依次与符合条件的元素相加，得到最终和。
     * </p>
     */
    public static void demonstrateStreamAPI() {
        System.out.println("\n===== Stream API 与 Lambda =====");

        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // filter() 中间操作：使用 Predicate 过滤出偶数
        // n -> n % 2 == 0 是一个 Predicate，只保留 2、4、6、8、10
        // collect() 终端操作：将 Stream 中剩余的元素收集到 List 中
        // Collectors.toList() 是一个预定义的 Collector，生成 ArrayList
        List<Integer> evens = numbers.stream()
                                    .filter(n -> n % 2 == 0)
                                    .collect(Collectors.toList());
        System.out.println("偶数: " + evens);

        // map() 中间操作：使用 Function 将每个元素转换为其平方值
        // n -> n * n 将 [1,2,3,...,10] 变为 [1,4,9,...,100]
        List<Integer> squared = numbers.stream()
                                      .map(n -> n * n)
                                      .collect(Collectors.toList());
        System.out.println("平方: " + squared);

        // filter() + reduce() 组合使用：先过滤后累加
        // 第一步 filter(n -> n > 5)：筛选出 6,7,8,9,10
        // 第二步 reduce(0, Integer::sum)：
        //   初始值 0 + 6 = 6 → 6 + 7 = 13 → 13 + 8 = 21 → 21 + 9 = 30 → 30 + 10 = 40
        int sum = numbers.stream()
                        .filter(n -> n > 5)
                        .reduce(0, Integer::sum);
        System.out.println("大于5的数字之和: " + sum);
    }

    /**
     * 演示自定义函数式接口的使用。
     *
     * <h2>{@code @FunctionalInterface} 的作用</h2>
     * <p>
     * {@code @FunctionalInterface} 注解标记一个接口为"函数式接口"。
     * 它不是必须的，但有以下重要作用：
     * </p>
     * <ul>
     *   <li><b>编译期检查：</b>编译器会验证该接口是否真的只有一个抽象方法。
     *       如果接口中有多个抽象方法，编译器会直接报错。</li>
     *   <li><b>文档意图：</b>明确告诉开发者"这个接口就是为 Lambda 表达式设计的"。</li>
     *   <li><b>防止误改：</b>如果未来有人不小心添加了第二个抽象方法，
     *       编译器会阻止，避免破坏 Lambda 兼容性。</li>
     * </ul>
     *
     * <h2>自定义 {@code Processor<T, R>} 接口</h2>
     * <p>
     * 这是一个泛型函数式接口，类型参数 T 是输入类型，R 是输出类型。
     * 抽象方法 {@code process(T input)} 接收 T 类型的输入，返回 R 类型的结果。
     * 本质上和内置的 {@code Function<T, R>} 功能相同，但通过自定义接口
     * 可以提供更有语义化的方法名和更清晰的用途表达。
     * </p>
     */
    public static void demonstrateCustomFunctionalInterface() {
        System.out.println("\n===== 自定义函数式接口 =====");

        // Processor<Integer, Integer>：输入 Integer，输出 Integer
        // 用 Lambda 表达式实现 Processor 接口的抽象方法 process()
        // n -> n * 2 定义了处理逻辑：将输入数字翻倍
        Processor<Integer, Integer> doubler = n -> n * 2;
        System.out.println("10 的两倍: " + doubler.process(10));

        // Processor<String, Integer>：输入 String，输出 Integer
        // 用方法引用 str::length 同理
        Processor<String, Integer> stringLength = str -> str.length();
        System.out.println("'Hello' 的长度: " + stringLength.process("Hello"));

        // 链式处理：手动组合两个 Processor
        // doubler.process(n) 先将输入翻倍，再交给 addThen.process() 加 100
        // 这是函数组合的雏形，最终的 chain 处理器实现了"先翻倍再加100"
        Processor<Integer, Integer> addThen = n -> n + 100;
        Processor<Integer, Integer> chain = n -> addThen.process(doubler.process(n));
        System.out.println("10 经过链式处理 (先翻倍后加100): " + chain.process(10));
    }

    /**
     * 演示高阶函数（Higher-Order Function）。
     *
     * <h2>什么是高阶函数？</h2>
     * <p>
     * 高阶函数是指满足以下至少一个条件的函数：
     * </p>
     * <ul>
     *   <li><b>接受函数作为参数：</b>本方法中的 {@code applyFunction} 就是一个例子</li>
     *   <li><b>返回一个函数：</b>本方法中的 {@code multiplier} 是一个例子</li>
     * </ul>
     * <p>
     * 高阶函数是函数式编程的核心特征，也是函数成为"一等公民"的体现。
     * </p>
     *
     * <h2>函数作为返回值的例子</h2>
     * <pre>{@code
     * Function<Integer, Function<Integer, Integer>> multiplier = x -> y -> x * y;
     * }</pre>
     * <p>
     * 这段代码表示一个接收整数 x 的函数，它返回 <b>另一个函数</b>（接收整数 y，返回 x * y）。
     * 这种技术称为 <b>柯里化（Currying）</b>——将一个多参数函数转换为
     * 一系列单参数函数的链式调用。
     * </p>
     * <p>
     * 过程分解：
     * </p>
     * <ol>
     *   <li>{@code multiplier.apply(3)} 返回一个函数 {@code y -> 3 * y}</li>
     *   <li>{@code multiplyBy3.apply(5)} 调用返回的函数，得到 {@code 3 * 5 = 15}</li>
     * </ol>
     */
    public static void demonstrateHigherOrderFunctions() {
        System.out.println("\n===== 高阶函数 =====");

        // 返回函数的函数（柯里化示例）
        // Function<Integer, Function<Integer, Integer>>：
        //   接收 Integer，返回 Function<Integer, Integer>（一个函数）
        // x -> y -> x * y：
        //   输入 x 后，返回一个新函数 y -> x * y
        //   这个新函数"记住"了 x 的值（闭包特性）
        Function<Integer, Function<Integer, Integer>> multiplier =
            x -> y -> x * y;

        // multiplier.apply(3) 返回一个函数 Function<Integer, Integer>
        // 这个函数等价于 y -> 3 * y
        Function<Integer, Integer> multiplyBy3 = multiplier.apply(3);
        // multiplyBy3.apply(5) 得到 3 * 5 = 15
        System.out.println("使用高阶函数计算 5 * 3: " + multiplyBy3.apply(5));

        // 接受函数作为参数的高阶函数调用
        // applyFunction() 接收一个列表和一个转换函数作为参数
        // 这里传入 n -> n * n（平方函数）作为转换逻辑
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> result = applyFunction(numbers, n -> n * n);
        System.out.println("应用平方函数: " + result);
    }

    /**
     * 辅助方法：高阶函数示例——接受函数作为参数。
     *
     * <p>
     * 这个方法本身就是一个高阶函数，因为它接受一个 {@code Function<Integer, Integer>}
     * 作为参数。调用者可以传入任何满足 Function 接口的行为（平方、加倍等），
     * 实现了"策略模式"的效果。
     * </p>
     *
     * <h2>Stream.map() 也是高阶函数</h2>
     * <p>
     * {@code list.stream().map(func)} 中的 {@code map()} 方法也接受一个
     * {@code Function} 参数，因此它本身也是一个高阶函数。
     * 本方法的作用是：将给定的函数应用到列表的每个元素上，返回新的列表。
     * </p>
     *
     * @param list 要进行转换的整数列表
     * @param func 要应用在每个元素上的转换函数，接收 Integer 返回 Integer
     * @return 经过函数转换后的新整数列表
     */
    private static List<Integer> applyFunction(List<Integer> list,
                                                Function<Integer, Integer> func) {
        // func::apply 是方法引用，等价于 num -> func.apply(num)
        // stream().map(func) 对每个元素应用 func 函数
        // collect(Collectors.toList()) 将结果收集到新列表中
        return list.stream()
                  .map(func)
                  .collect(Collectors.toList());
    }
}
