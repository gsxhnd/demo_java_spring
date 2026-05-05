package com.example.web.exception;

import com.example.web.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 全局异常处理器 — 统一处理应用中所有未捕获的异常。
 *
 * <h2>核心概念：@ControllerAdvice + @ExceptionHandler 全局异常处理机制</h2>
 *
 * <h3>问题的提出</h3>
 * <p>在没有全局异常处理器的情况下，如果每个 Controller 方法都自己 try-catch 异常并构造错误响应，
 * 会导致：</p>
 * <ul>
 *   <li>大量重复的异常处理代码散落在各个控制器中</li>
 *   <li>错误响应格式不一致（有的返回 JSON，有的返回 HTML 错误页）</li>
 *   <li>错误处理逻辑和业务逻辑耦合，违反单一职责原则</li>
 * </ul>
 *
 * <h3>@ControllerAdvice — 全局控制器增强</h3>
 * <p>@ControllerAdvice 是 Spring MVC 提供的 AOP（面向切面编程）机制，它会对<b>所有 @Controller /
 * @RestController 标注的类</b>进行增强。可以理解为在控制器方法执行的前后插入额外的处理逻辑。
 * 默认作用于所有控制器，也可通过 basePackages、annotations 等属性限定作用范围。</p>
 *
 * <p>@ControllerAdvice 可以定义三种处理逻辑：</p>
 * <ol>
 *   <li>@ExceptionHandler — 异常处理（本类使用的功能）</li>
 *   <li>@InitBinder — 数据绑定初始化</li>
 *   <li>@ModelAttribute — 公共模型属性</li>
 * </ol>
 *
 * <h3>@ExceptionHandler — 特定异常的处理方法</h3>
 * <p>@ExceptionHandler 标注在 @ControllerAdvice（或 @Controller）类的方法上，
 * 定义该方法负责处理哪种类型的异常。Spring 在捕获到异常后，会根据异常的运行时类型
 * 匹配最合适的 @ExceptionHandler 方法（遵循 Java 异常继承树的就近匹配规则）。</p>
 *
 * <h3>异常匹配的优先级</h3>
 * <p>当异常发生时，Spring 按以下顺序查找处理器：</p>
 * <ol>
 *   <li>当前 Controller 中的 @ExceptionHandler 方法（局部处理器）</li>
 *   <li>@ControllerAdvice 类中的 @ExceptionHandler 方法（全局处理器，本类即是）</li>
 *   <li>Spring Boot 内置的 BasicErrorController（最终兜底）</li>
 * </ol>
 * <p>本类中的三个 @ExceptionHandler 方法，Spring 会按照异常类型进行匹配：</p>
 * <ul>
 *   <li>UserNotFoundException → handleUserNotFoundException()</li>
 *   <li>MethodArgumentNotValidException → handleValidationException()</li>
 *   <li>Exception（所有未匹配的异常） → handleGlobalException()</li>
 * </ul>
 *
 * <h3>处理流程示意</h3>
 * <pre>{@code
 * 客户端请求 → DispatcherServlet → Controller → Service（抛出异常）
 *                                                    ↓
 *         JSON 错误响应 ← @ExceptionHandler  ← 异常向上传播被拦截
 * }</pre>
 *
 * @author Spring Demo Team
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理用户不存在的业务异常 → HTTP 404 Not Found。
     *
     * <h2>@ExceptionHandler 注解详解</h2>
     * <p><code>@ExceptionHandler(UserNotFoundException.class)</code> 告诉 Spring：
     * 当任何 Controller 抛出 UserNotFoundException 时，调用此方法来处理。</p>
     *
     * <h2>方法参数：WebRequest</h2>
     * <p>{@link WebRequest} 是 Spring 提供的请求上下文抽象，可以获取请求的 URI、
     * 请求参数、Session 等信息。这里使用 <code>request.getDescription(false)</code>
     * 获取请求描述（格式如 "uri=/api/users/abc123"），再截取出路径部分填充到 ErrorResponse 中。</p>
     *
     * <h2>@Builder 模式构造 ErrorResponse</h2>
     * <p>使用 <code>ErrorResponse.builder()...build()</code> 流式构造错误响应对象。
     * 与传统的 setter 逐个赋值相比，建造者模式的链式调用更清晰紧凑。
     * 注意：建造者模式创建的对象通常是<b>不可变的</b>（没有 setter），
     * 但这里因为 @Data 也生成了 setter，所以实际上是可变对象。</p>
     *
     * @param ex      捕获到的 UserNotFoundException，通过 ex.getMessage() 获取错误消息
     * @param request 当前 Web 请求上下文，用于提取请求路径写入 ErrorResponse
     * @return ResponseEntity 包含：404 状态码 + ErrorResponse JSON body
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {
        // 记录完整的异常堆栈，便于后端排查问题
        log.error("用户不存在异常", ex);

        /*
         * 使用 @Builder 模式构造 ErrorResponse：
         * - code: 404（来自 HttpStatus.NOT_FOUND.value()）
         * - message: 异常的 message（如 "用户不存在: abc123"）
         * - detail: 面向开发者的补充说明
         * - path: 触发异常的请求路径（如 /api/users/abc123）
         * - timestamp: 异常发生的精确时刻
         */
        ErrorResponse error = ErrorResponse.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .detail("请求的用户资源不存在")
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();

        /*
         * new ResponseEntity<>(body, status) 同时设置响应体和 HTTP 状态码。
         * 也可使用快捷方法：ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
         */
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * 处理参数校验失败异常 → HTTP 400 Bad Request。
     *
     * <h2>MethodArgumentNotValidException — 何时触发？</h2>
     * <p>当控制器方法参数上标注了 @Valid，且 Jakarta Bean Validation 校验失败时，
     * Spring 抛出 {@link MethodArgumentNotValidException}。这是 Spring 层面的异常，
     * 包装了 Jakarta Validation 的 ConstraintViolation 信息。</p>
     *
     * <h2>getBindingResult().getFieldErrors() — 提取字段级错误</h2>
     * <p>MethodArgumentNotValidException 包含了详细的校验失败信息：</p>
     * <ul>
     *   <li><code>ex.getBindingResult()</code> — 获取绑定/校验结果对象</li>
     *   <li><code>.getFieldErrors()</code> — 获取所有字段级别的错误列表（List&lt;FieldError&gt;）</li>
     *   <li>每个 FieldError 包含：字段名（getField()）、被拒绝的值（getRejectedValue()）、
     *   错误消息（getDefaultMessage()）</li>
     * </ul>
     *
     * <h2>Stream API 聚合错误信息</h2>
     * <p>使用 Java 8 Stream 将多个字段错误聚合成一条逗号分隔的消息：</p>
     * <pre>{@code
     * fieldErrors.stream()
     *     .map(error -> error.getField() + ": " + error.getDefaultMessage())
     *     .collect(Collectors.joining(", "));
     * // 结果类似: "username: 用户名长度必须在3-20个字符之间, email: 邮箱格式不正确"
     * }</pre>
     *
     * @param ex      捕获到的参数校验异常，包含所有字段校验失败的信息
     * @param request 当前请求上下文
     * @return ResponseEntity 包含：400 状态码 + 详细的校验失败描述
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.error("参数校验异常", ex);

        /*
         * 遍历所有字段校验错误，拼接成人类可读的字符串。
         * 例如："username: 用户名长度必须在3-20个字符之间, email: 邮箱格式不正确"
         */
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse error = ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())      // 400
                .message("请求参数验证失败")                  // 面向用户的简短消息
                .detail(message)                             // 面向开发者的详细字段错误
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();

        // 返回 400 Bad Request
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * 兜底异常处理器 — 捕获所有未被前面方法匹配的异常 → HTTP 500 Internal Server Error。
     *
     * <h2>Exception.class — 最宽泛的匹配</h2>
     * <p><code>@ExceptionHandler(Exception.class)</code> 会匹配所有 Exception 及其子类。
     * 由于 Spring 按就近匹配规则，优先匹配更具体的异常处理器
     * （如 UserNotFoundException、MethodArgumentNotValidException），
     * 只有当抛出的异常不属于前两种类型时，才会落到此方法中。</p>
     *
     * <h2>安全考虑</h2>
     * <p>在生产环境中，此方法通常需要更谨慎地处理异常详情：</p>
     * <ul>
     *   <li>不应将 <code>ex.getMessage()</code> 直接暴露给客户端，
     *   因为可能包含敏感信息（SQL 错误、文件路径、IP 地址等）</li>
     *   <li>应使用统一的 "系统内部错误" 作为 message，并将原始异常详情写入服务端日志</li>
     *   <li>本学习项目中为了教学目的保留 detail 字段展示异常消息</li>
     * </ul>
     *
     * @param ex      任何未被前面处理器捕获的异常
     * @param request 当前请求上下文
     * @return ResponseEntity 包含：500 状态码 + 通用错误消息
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        /*
         * log.error 记录完整堆栈到服务端日志，便于运维排查。
         * 客户端只收到简化的错误描述，不暴露内部实现细节。
         */
        log.error("系统异常", ex);

        ErrorResponse error = ErrorResponse.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())  // 500
                .message("系统内部错误")                           // 面向用户的通用消息
                .detail(ex.getMessage())                          // 开发调试信息（生产环境应控制）
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
