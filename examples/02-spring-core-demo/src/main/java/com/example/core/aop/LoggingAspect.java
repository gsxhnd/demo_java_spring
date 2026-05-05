package com.example.core.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * 日志切面 — 全面演示 Spring AOP（Aspect-Oriented Programming，面向切面编程）的五种通知类型。
 *
 * <h3>AOP 核心概念总览：</h3>
 * <table border="1">
 *   <tr><th>概念</th><th>本类对应</th><th>说明</th></tr>
 *   <tr><td>Aspect（切面）</td><td>LoggingAspect 类</td><td>切面 = 切入点 + 通知，是横切关注点的模块化</td></tr>
 *   <tr><td>Pointcut（切入点）</td><td>businessMethods() 方法</td><td>定义"在哪里"执行切面逻辑</td></tr>
 *   <tr><td>Advice（通知）</td><td>beforeMethod / afterMethod 等方法</td><td>定义"什么时候"和"做什么"</td></tr>
 *   <tr><td>JoinPoint（连接点）</td><td>方法参数 JoinPoint</td><td>程序执行过程中的某个点，如方法调用</td></tr>
 *   <tr><td>Weaving（织入）</td><td>Spring 自动完成</td><td>将切面逻辑应用到目标对象上的过程</td></tr>
 * </table>
 *
 * <h3>@Aspect 注解详解：</h3>
 * <p>标记一个类为"切面类"。需要注意的是，仅有 @Aspect 还不够 ——
 * 还需要将该类注册为 Spring Bean（通过 @Component），
 * 并且需要在配置类上开启 {@code @EnableAspectJAutoProxy}，
 * 这样 Spring 才会在容器启动时为匹配的目标对象创建代理，将切面逻辑织入。</p>
 *
 * <h3>为什么不直接注册为 Bean 就可以？</h3>
 * <p>@Aspect 本身不会被 Spring 自动扫描为 Bean。必须配合 @Component（或通过 @Bean 方法手动注册）
 * 才能被 Spring 容器管理。如果没有 @Component，切面类即使定义了也不会生效。</p>
 *
 * <h3>AOP 底层实现原理（重要）：</h3>
 * <p>Spring AOP 基于<em>动态代理</em>实现，有两种代理方式：</p>
 * <ol>
 *   <li><b>JDK 动态代理</b>：目标类实现了接口时优先使用。基于 java.lang.reflect.Proxy，
 *       代理对象和目标对象实现同一接口。</li>
 *   <li><b>CGLIB 代理</b>：目标类没有实现接口时使用。通过继承目标类生成子类来实现代理。</li>
 * </ol>
 * <p>当方法调用发生时，实际调用的是代理对象的方法，代理对象在调用目标方法前后执行切面逻辑。</p>
 *
 * <h3>切入点表达式（Pointcut Expression）详解：</h3>
 * <pre>{@code execution(public * com.example.core.business..*(..))}</pre>
 * <table border="1">
 *   <tr><th>部分</th><th>含义</th></tr>
 *   <tr><td>{@code execution}</td><td>表达式类型：方法执行时匹配</td></tr>
 *   <tr><td>{@code public}</td><td>访问修饰符：只匹配 public 方法</td></tr>
 *   <tr><td>{@code *}</td><td>返回类型：任意返回类型</td></tr>
 *   <tr><td>{@code com.example.core.business}</td><td>包路径：指定拦截的包</td></tr>
 *   <tr><td>{@code ..}</td><td>包含当前包及其所有子包</td></tr>
 *   <tr><td>{@code *}</td><td>方法名通配符：任意方法名</td></tr>
 *   <tr><td>{@code (..)}</td><td>参数通配符：任意参数类型和数量</td></tr>
 * </table>
 *
 * <h3>五种通知类型总结：</h3>
 * <pre>
 *   @Around  ─┐
 *   @Before   ├─ 目标方法执行前
 *             │
 *   ===== 目标方法执行 =====
 *             │
 *   @AfterReturning ─┤ 目标方法正常返回后
 *   @AfterThrowing  ─┤ 目标方法抛出异常后
 *   @After          ─┘ 目标方法结束后（无论是否有异常）
 *   @Around  ───────┘ @Around 的后半部分也在这里执行
 * </pre>
 */
@Slf4j
@Aspect      // 【核心注解】声明此类是一个切面类（Aspect）
@Component   // 【核心注解】将切面类注册为 Spring Bean（@Aspect 不会自动注册）
public class LoggingAspect {

    /**
     * 【切入点定义】定义在哪些方法上应用切面逻辑。
     *
     * <p>本切入点匹配 com.example.core.business 包及其所有子包中
     * 所有 public 方法的所有执行。该方法体为空，因为切入点表达式已经完整定义了
     * 匹配规则，方法名（businessMethods）仅作为切入点的引用标识。</p>
     *
     * <p>切入点支持通过 {@code &&}、{@code ||}、{@code !} 进行逻辑组合，
     * 例如：{@code @Pointcut("execution(* *(..)) && @annotation(org.springframework.transaction.annotation.Transactional)")}</p>
     */
    @Pointcut("execution(public * com.example.core.business..*(..))")
    public void businessMethods() {
        // 切入点方法体为空，仅作为 @Before/@After 等注解的引用标识
    }

    /**
     * 【前置通知 @Before】在目标方法执行前运行。
     *
     * <p>无论目标方法是否成功执行，@Before 都会运行。
     * 如果 @Before 中抛出异常，目标方法将不会被执行（除非 @Around 捕获了该异常）。</p>
     *
     * <p>JoinPoint 参数提供了切入点的反射信息：</p>
     * <ul>
     *   <li>{@code joinPoint.getSignature()} — 方法签名（名称、修饰符等）</li>
     *   <li>{@code joinPoint.getArgs()} — 目标方法的参数数组</li>
     *   <li>{@code joinPoint.getTarget()} — 被代理的目标对象</li>
     *   <li>{@code joinPoint.getThis()} — 当前的代理对象</li>
     * </ul>
     *
     * @param joinPoint 连接点对象，包含被拦截方法的所有反射信息
     */
    @Before("businessMethods()")     // 引用上面定义的切入点
    public void beforeMethod(JoinPoint joinPoint) {
        log.info("【前置通知】即将执行方法: {}", joinPoint.getSignature().getName());
        Object[] args = joinPoint.getArgs(); // 获取目标方法的所有参数
        if (args.length > 0) {
            log.info("【前置通知】方法参数: {}", (Object) args);
        }
    }

    /**
     * 【后置通知 @After】在目标方法执行完成后运行，无论方法是正常返回还是抛出异常。
     *
     * <p>@After 有点类似于 Java 的 finally 块：</p>
     * <ul>
     *   <li>目标方法正常执行完成 → @After 仍会执行</li>
     *   <li>目标方法抛出异常 → @After 仍会执行</li>
     *   <li>@Before 中抛出异常 → @After 不会执行（因为目标方法根本没被调用）</li>
     * </ul>
     *
     * <p>适用场景：释放资源（如关闭文件流）、记录方法执行日志、清理 ThreadLocal 等。</p>
     *
     * @param joinPoint 连接点对象，包含被拦截方法的信息
     */
    @After("businessMethods()")
    public void afterMethod(JoinPoint joinPoint) {
        log.info("【后置通知】方法执行完毕: {}", joinPoint.getSignature().getName());
    }

    /**
     * 【返回通知 @AfterReturning】仅在目标方法<em>正常返回（无异常）</em>时运行。
     *
     * <p>关键属性：</p>
     * <ul>
     *   <li>{@code pointcut}：引用的切入点表达式</li>
     *   <li>{@code returning}：指定接收返回值的参数名称，
     *       该名称必须与方法参数名一致（本例中为 "result"）</li>
     * </ul>
     *
     * <p>适用场景：对返回值做后处理（如数据脱敏、格式转换、缓存结果）、
     * 性能监控（记录方法执行耗时）、业务审计等。</p>
     *
     * @param joinPoint 连接点对象
     * @param result    目标方法的返回值，由 returning 属性指定参数名
     */
    @AfterReturning(pointcut = "businessMethods()", returning = "result")
    public void afterReturningMethod(JoinPoint joinPoint, Object result) {
        log.info("【返回通知】方法正常返回: {}, 返回值: {}",
                 joinPoint.getSignature().getName(), result);
    }

    /**
     * 【异常通知 @AfterThrowing】仅在目标方法<em>抛出异常</em>时运行。
     *
     * <p>关键属性：</p>
     * <ul>
     *   <li>{@code pointcut}：引用的切入点表达式</li>
     *   <li>{@code throwing}：指定接收异常对象的参数名，
     *       该名称必须与方法参数名一致（本例中为 "exception"）</li>
     * </ul>
     *
     * <p>适用场景：统一异常日志记录、异常告警、异常转换（将低级异常包装为业务异常）、
     * 记录错误堆栈用于排查问题等。</p>
     *
     * <p>注意：@AfterThrowing 不会吞掉异常，异常会继续向上抛出。</p>
     *
     * @param joinPoint 连接点对象
     * @param exception 目标方法抛出的异常对象，由 throwing 属性指定参数名
     */
    @AfterThrowing(pointcut = "businessMethods()", throwing = "exception")
    public void afterThrowingMethod(JoinPoint joinPoint, Exception exception) {
        log.error("【异常通知】方法执行异常: {}, 异常: {}",
                  joinPoint.getSignature().getName(), exception.getMessage());
    }

    /**
     * 【环绕通知 @Around】最强大的通知类型，可以完全控制目标方法的执行。
     *
     * <p>与其他四种通知的本质区别：</p>
     * <ul>
     *   <li>其他通知是被动接收回调的，无法控制目标方法是否执行。</li>
     *   <li>@Around 通过 {@code joinPoint.proceed()} 显式决定是否调用目标方法，
     *       可以在调用前后/异常时自行编写任何逻辑。</li>
     * </ul>
     *
     * <p>使用 ProceedingJoinPoint（JoinPoint 的子接口）：</p>
     * <ul>
     *   <li>{@code joinPoint.proceed()} — 执行目标方法（必须显式调用！）</li>
     *   <li>{@code joinPoint.proceed(Object[])} — 修改参数后再执行目标方法</li>
     *   <li>如果不调用 proceed()，目标方法就不会被执行</li>
     * </ul>
     *
     * <p>典型应用场景：</p>
     * <ul>
     *   <li>性能监控（记录方法执行耗时）</li>
     *   <li>事务管理（proceed 前开启事务，异常时回滚，正常时提交）</li>
     *   <li>缓存（proceed 前检查缓存，命中则直接返回缓存值，不调用 proceed）</li>
     *   <li>重试机制（proceed 抛出异常时循环重试）</li>
     *   <li>权限校验（校验通过才调用 proceed）</li>
     * </ul>
     *
     * <p><b>注意：</b>必须返回 proceed() 的返回值，否则调用方收到的将是 null。
     * 如果目标方法返回类型是 void，则返回 null 即可。</p>
     *
     * @param joinPoint 环绕连接点，比 JoinPoint 多了 proceed() 方法
     * @return 目标方法的返回值（必须返回，否则调用方收到 null）
     * @throws Throwable 将目标方法的异常原样抛出，让上层调用方处理
     */
    @Around("businessMethods()")
    public Object aroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis(); // 记录方法执行前的时间戳，用于计算耗时
        log.info("【环绕通知】方法执行前: {}", joinPoint.getSignature().getName());

        try {
            // ★ 核心步骤：调用 proceed() 执行目标方法
            // 如果不调用这行代码，目标方法就不会被执行
            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - startTime; // 计算执行耗时
            log.info("【环绕通知】方法执行后，耗时: {}ms", duration);

            return result; // ★ 必须将目标方法的返回值返回，否则调用方收到 null
        } catch (Throwable e) {
            log.error("【环绕通知】方法执行异常", e);
            throw e; // 将异常重新抛出，不吞掉异常
        }
    }
}
