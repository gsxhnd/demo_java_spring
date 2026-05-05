package com.example.basics.lambda;

/**
 * 自定义函数式接口——演示 {@code @FunctionalInterface} 注解的使用。
 *
 * <h2>什么是函数式接口？</h2>
 * <p>
 * 函数式接口（Functional Interface）是指 <b>有且仅有一个抽象方法</b> 的接口。
 * 这种接口可以用 Lambda 表达式来实例化，因为 Lambda 表达式的唯一作用就是
 * 实现那个唯一的抽象方法。
 * </p>
 * <p>
 * 接口中可以有多个 <b>默认方法</b>（default method）和 <b>静态方法</b>（static method），
 * 这些不算在"抽象方法"计数内。只有没有方法体的抽象方法才计入计数。
 * </p>
 *
 * <h2>{@code @FunctionalInterface} 注解详解</h2>
 * <p>
 * 这是一个 <b>标记性注解</b>（marker annotation），它不做任何运行时行为改变，
 * 但有以下重要用途：
 * </p>
 *
 * <h3>1. 编译期校验（最重要！）</h3>
 * <p>
 * 编译器会检查被 {@code @FunctionalInterface} 标记的接口是否确实只有一个抽象方法。
 * 如果接口中有 0 个或 2 个及以上的抽象方法，<b>编译器直接报错</b>。
 * 这是最核心的用途——防止开发者不小心破坏了接口的函数式特性。
 * </p>
 *
 * <h3>2. 文档意图</h3>
 * <p>
 * 明确告知代码阅读者：这个接口是为 Lambda 表达式设计的，应该用 Lambda 来使用它，
 * 而不应该用匿名内部类（虽然技术上可行）。
 * </p>
 *
 * <h3>3. 与 Java 内置函数式接口的关系</h3>
 * <p>
 * 这个 {@code Processor<T, R>} 接口的功能与 Java 内置的
 * {@code java.util.function.Function<T, R>} 完全等价——两者都接收 T 类型的输入，
 * 返回 R 类型的结果。但自定义函数式接口的优势在于：
 * </p>
 * <ul>
 *   <li>提供更有语义化的方法名（{@code process} vs {@code apply}）</li>
 *   <li>可以添加额外的文档注释说明具体用途</li>
 *   <li>限定使用场景，使代码意图更明确</li>
 * </ul>
 *
 * <h2>类型参数说明</h2>
 * <ul>
 *   <li>{@code <T>}（Type）：输入参数的类型。可以是任何引用类型，
 *       如 String、Integer、自定义类等</li>
 *   <li>{@code <R>}（Result）：返回结果的类型。可以是任何引用类型，
 *       可以和 T 相同也可以不同</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 使用 Lambda 表达式实现 Processor
 * Processor<Integer, Integer> doubler = n -> n * 2;
 * doubler.process(10); // 返回 20
 * }</pre>
 *
 * <h2>注意</h2>
 * <p>
 * {@code @FunctionalInterface} 注解是 <b>可选的</b>——即使不加这个注解，
 * 只要接口只有一个抽象方法，它仍然是函数式接口，仍可以使用 Lambda 表达式。
 * 但强烈建议在所有函数式接口上都加上此注解，以获得编译期检查和文档提示的好处。
 * </p>
 *
 * @param <T> 输入参数的类型
 * @param <R> 处理结果的类型
 *
 * @see java.util.function.Function
 * @see java.util.function.Consumer
 * @see java.util.function.Supplier
 * @see java.util.function.Predicate
 */
@FunctionalInterface  // 编译期校验：此接口有且仅有一个抽象方法
public interface Processor<T, R> {

    /**
     * 处理输入数据并返回结果。
     *
     * <p>
     * 这是该函数式接口的 <b>唯一抽象方法</b>（Single Abstract Method，简称 SAM）。
     * 当使用 Lambda 表达式实例化该接口时，Lambda 的签名必须与此方法匹配：
     * 接收一个 T 类型的参数，返回一个 R 类型的结果。
     * </p>
     *
     * <p>
     * 如果你看到 {@code Processor<Integer, String> p = n -> "Number: " + n;}，
     * 那么 n 是 Integer 类型（对应 T），Lambda 返回的字符串是 R 类型。
     * </p>
     *
     * @param input 需要处理的数据，类型为 T
     * @return 处理后的结果，类型为 R
     */
    R process(T input);
}
