package com.example.web.exception;

/**
 * 业务异常 — 用户不存在。
 *
 * <h2>核心概念：自定义业务异常的设计目的</h2>
 * <p>在 Spring 应用中，将业务错误封装为特定的异常类（而非返回错误码或 null）有以下优势：</p>
 * <ol>
 *   <li><b>明确的异常类型</b>：抛出 UserNotFoundException 比抛出 RuntimeException("用户不存在")
 *   更精确。全局异常处理器可以根据异常类型（而不只是异常消息字符串）来决定 HTTP 状态码和错误格式。</li>
 *   <li><b>向上传播</b>：异常可以在服务层抛出，穿过控制器层，最终由全局异常处理器统一捕获。
 *   控制器层代码无需写 try-catch，保持干净。</li>
 *   <li><b>语义清晰</b>：异常类名本身就说明了问题是什么，
 *   提高代码可读性（"代码即文档"）。</li>
 *   <li><b>可扩展</b>：未来可以添加额外的上下文信息（如出错的 ID、操作类型等），
 *   帮助异常处理器生成更详细的错误响应。</li>
 * </ol>
 *
 * <h2>继承 RuntimeException 还是 Exception？</h2>
 * <p>这里继承 {@link RuntimeException}（运行时异常）而非受检异常（{@link Exception}）：</p>
 * <ul>
 *   <li><b>运行时异常</b> — Spring 的声明式事务管理默认只对 RuntimeException 回滚；
 *   而且方法签名中无需声明 throws，代码更简洁。</li>
 *   <li><b>受检异常</b> — 强制调用者处理，适合调用方能够恢复的错误（如网络超时可重试）。
 *   但对于"用户不存在"这类不可恢复的业务错误，运行时异常更合适。</li>
 * </ul>
 *
 * <h2>异常处理流程</h2>
 * <ol>
 *   <li>{@link com.example.web.service.UserService} 中检测到用户不存在时，
 *   抛出 <code>throw new UserNotFoundException("用户不存在: " + id)</code></li>
 *   <li>异常从服务层向上传播，经过控制器层（控制器不需要捕获）</li>
 *   <li>{@link GlobalExceptionHandler#handleUserNotFoundException} 捕获此异常</li>
 *   <li>生成包含 404 状态码的 ErrorResponse JSON，返回给客户端</li>
 * </ol>
 *
 * @author Spring Demo Team
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * 构造方法 — 创建带有描述消息的异常。
     *
     * @param message 异常描述，通常会包含导致异常的具体信息（如出错的用户 ID），
     *                这个信息最终会出现在 API 的 ErrorResponse 中
     */
    public UserNotFoundException(String message) {
        /*
         * super(message) 调用父类 RuntimeException 的构造方法，
         * 将 message 存入异常对象的 detailMessage 字段中。
         * 后续通过 ex.getMessage() 即可获取此消息。
         */
        super(message);
    }

    /**
     * 构造方法 — 创建带有描述消息和原始原因的异常（异常链）。
     *
     * <h2>异常链（Exception Chaining）</h2>
     * <p>当一个异常是由另一个异常触发的（如数据库连接失败导致查询不到用户），
     * 使用此构造方法可以将原始异常设置为 cause。好处：</p>
     * <ul>
     *   <li>日志中可以看到完整的异常调用栈，便于排查根因</li>
     *   <li>不丢失原始异常信息（数据库驱动的具体错误消息等）</li>
     * </ul>
     *
     * @param message 异常描述
     * @param cause   引发此异常的根本原因（底层异常），可为 null
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
