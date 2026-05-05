package com.example.web.controller;

import com.example.web.dto.CreateUserRequest;
import com.example.web.dto.UserResponse;
import com.example.web.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户 REST 控制器，提供符合 REST 风格的用户管理 API。
 *
 * <h2>核心概念：@RestController 与 @Controller 的区别</h2>
 * <p><b>@RestController</b> = @Controller + @ResponseBody。</p>
 * <ul>
 *   <li><b>@Controller</b> — 标记一个类为 Spring MVC 的控制器，返回的通常是视图名（如 JSP/Thymeleaf 模板路径）。</li>
 *   <li><b>@RestController</b> — 是 @Controller 的增强版本，它的每个方法返回值都会自动以 JSON/XML 的形式
 *   写入 HTTP 响应体（Response Body），无需在每个方法上单独加 @ResponseBody。
 *   这正是 RESTful API 开发所需要的：返回数据，而非视图。</li>
 * </ul>
 *
 * <h2>核心概念：@RequestMapping("/api/users") — 类级别路径映射</h2>
 * <p>标注在类上的 @RequestMapping 为该控制器中所有接口方法定义了一个<b>公共的基础路径（base path）</b>。
 * 例如，本类中所有方法的 URL 都以 <code>/api/users</code> 开头，最终完整路径由"类路径 + 方法路径"拼接而成：</p>
 * <ul>
 *   <li>GET   <code>/api/users</code>        → getAllUsers()</li>
 *   <li>GET   <code>/api/users/{id}</code>   → getUserById()</li>
 *   <li>POST  <code>/api/users</code>        → createUser()</li>
 *   <li>PUT   <code>/api/users/{id}</code>   → updateUser()</li>
 *   <li>DELETE <code>/api/users/{id}</code>   → deleteUser()</li>
 * </ul>
 *
 * <h2>核心概念：构造器注入（Constructor Injection）</h2>
 * <p>使用 <code>private final</code> 字段 + 构造器注入，这是 Spring 推荐的依赖注入方式（优于 @Autowired 字段注入）：</p>
 * <ol>
 *   <li><b>不可变性</b>：final 字段确保依赖不会被修改</li>
 *   <li><b>可测试性</b>：单元测试时可直接通过构造器传入 mock 对象</li>
 *   <li><b>明确性</b>：构造器参数明确声明了类所依赖的全部组件</li>
 *   <li><b>避免 NPE</b>：Spring 4.3+ 对单构造器自动装配，无需显式 @Autowired</li>
 * </ol>
 *
 * <h2>Swagger/OpenAPI 注解说明</h2>
 * <ul>
 *   <li><b>@Tag</b> — 在 Swagger UI 中对控制器进行分组，name 属性为分组名称，description 为分组描述。</li>
 *   <li><b>@Operation</b> — 描述单个 API 接口，summary 为简短摘要，description 为详细说明。</li>
 *   <li><b>@ApiResponse</b> — 描述某个 HTTP 状态码下返回的响应格式。</li>
 *   <li><b>@ApiResponses</b> — 组合多个 @ApiResponse，表达该接口可能返回的多种响应状态。</li>
 * </ul>
 *
 * @author Spring Demo Team
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户相关的 API")
public class UserController {

    /**
     * 用户业务服务——通过构造器注入。
     * <p>Spring 会在创建 UserController 实例时，自动从 IoC 容器中找到 UserService 类型的 Bean 并传入。</p>
     */
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users — 查询所有用户。
     *
     * <h2>核心概念：@GetMapping — 映射 HTTP GET 请求</h2>
     * <p>@GetMapping 是 @RequestMapping(method = RequestMethod.GET) 的简写形式。
     * HTTP GET 方法在 REST 中代表"获取/查询资源"，具有以下特征：</p>
     * <ul>
     *   <li><b>幂等性</b>：多次相同的 GET 请求，返回结果应一致（不改变服务端状态）</li>
     *   <li><b>安全性</b>：GET 请求不应修改服务端的资源</li>
     *   <li><b>可缓存</b>：浏览器/代理可缓存 GET 响应</li>
     * </ul>
     *
     * <h2>核心概念：ResponseEntity — 控制 HTTP 响应</h2>
     * <p>ResponseEntity 是 Spring 提供的一个泛型类，用于<b>完全控制 HTTP 响应</b>，包括：</p>
     * <ul>
     *   <li><b>响应体（Body）</b>：调用方实际需要的数据，如 JSON 格式的用户列表</li>
     *   <li><b>HTTP 状态码（Status）</b>：如 200 OK、201 Created、404 Not Found 等</li>
     *   <li><b>HTTP 响应头（Headers）</b>：如 Content-Type、Cache-Control、Location 等</li>
     * </ul>
     * <p><code>ResponseEntity.ok(body)</code> 是创建状态码 200 响应的快捷方法，等价于：
     * <code>new ResponseEntity<>(body, HttpStatus.OK)</code>。</p>
     * <p>如果不使用 ResponseEntity 而直接返回对象，Spring 会自动将其序列化为 JSON 并设置 200 状态码，
     * 但 ResponseEntity 提供了更精细的控制能力。</p>
     *
     * @return 200 OK，响应体包含所有用户的列表
     */
    @GetMapping
    @Operation(summary = "获取所有用户", description = "返回系统中所有的用户列表")
    @ApiResponse(responseCode = "200", description = "成功获取用户列表")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("获取所有用户");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/{id} — 根据用户 ID 查询单个用户。
     *
     * <h2>核心概念：@PathVariable — 从 URL 路径中提取变量</h2>
     * <p>@PathVariable 用于将 URL 路径模板中的 {参数名} 动态绑定到方法参数上。
     * 例如，请求 <code>/api/users/abc123</code> 时，方法参数 id 的值就是 <code>"abc123"</code>。
     * 这是 RESTful 风格的核心机制之一——<b>资源标识符直接体现在 URL 路径中</b>。</p>
     *
     * <h2>路径变量的匹配规则</h2>
     * <ul>
     *   <li>如果 URL 模板中的变量名与参数名一致（如 {id} 对应 String id），可省略 @PathVariable 的 value 属性</li>
     *   <li>如果不一致，需要显式指定：<code>@PathVariable("id") String userId</code></li>
     *   <li>路径变量默认是必需的，缺少会导致 404 错误</li>
     * </ul>
     *
     * @param id URL 路径中的用户唯一标识符，由 @PathVariable 自动提取
     * @return 200 OK（用户存在时），响应体包含单个用户信息；404 Not Found（用户不存在时，由全局异常处理器返回）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据用户 ID 获取用户信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功获取用户"),
        @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable String id) {
        log.info("获取用户: {}", id);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * POST /api/users — 创建新用户。
     *
     * <h2>核心概念：@PostMapping — 映射 HTTP POST 请求</h2>
     * <p>@PostMapping 是 @RequestMapping(method = RequestMethod.POST) 的简写。
     * HTTP POST 方法在 REST 中代表"创建新资源"，特征：</p>
     * <ul>
     *   <li><b>非幂等</b>：多次相同的 POST 请求会创建多个资源（每次产生不同的 ID）</li>
     *   <li><b>不安全</b>：会修改服务端状态</li>
     *   <li>请求参数通常放在请求体中（而非 URL），适合传输大量/复杂数据</li>
     * </ul>
     *
     * <h2>核心概念：@RequestBody — 将 HTTP 请求体反序列化为 Java 对象</h2>
     * <p>@RequestBody 告诉 Spring：从 HTTP 请求的 Body 中读取 JSON（或 XML）字符串，
     * 并自动反序列化为指定类型的 Java 对象。Spring 默认使用 Jackson 库完成 JSON ↔ Java 的相互转换。</p>
     * <p>工作流程：</p>
     * <ol>
     *   <li>客户端发送 JSON 格式的请求体（Content-Type: application/json）</li>
     *   <li>Spring 的 MappingJackson2HttpMessageConverter 拦截请求</li>
     *   <li>Jackson 将 JSON 字符串映射为 CreateUserRequest 对象（根据字段名匹配）</li>
     *   <li>方法内部直接使用已填充好的 Java 对象</li>
     * </ol>
     *
     * <h2>核心概念：@Valid — 触发 Jakarta Bean Validation 校验</h2>
     * <p>@Valid 注解指示 Spring 在绑定请求体后、调用方法前，对 CreateUserRequest 对象执行 Jakarta Bean Validation。
     * 校验规则由该 DTO 类中字段上的注解定义（如 @NotBlank、@Email、@Min/@Max 等）。
     * 如果校验失败，Spring 会抛出 {@link org.springframework.web.bind.MethodArgumentNotValidException}，
     * 由 {@link com.example.web.exception.GlobalExceptionHandler} 统一捕获并返回 400 错误响应。</p>
     *
     * <h2>REST 创建资源的约定：返回 201 Created</h2>
     * <p>REST 规范建议创建成功的响应使用 HTTP 201 Created 状态码（而非 200 OK）。
     * 这里通过 <code>ResponseEntity.status(HttpStatus.CREATED).body(user)</code> 显式设置 201。</p>
     *
     * @param request 包含创建用户所需信息的请求体，由 @RequestBody 反序列化得到
     * @return 201 Created，响应体包含新创建的用户信息
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建一个新的用户")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "用户创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数验证失败")
    })
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        log.info("创建用户: {}", request.getUsername());
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * PUT /api/users/{id} — 更新指定用户。
     *
     * <h2>核心概念：@PutMapping — 映射 HTTP PUT 请求</h2>
     * <p>@PutMapping 是 @RequestMapping(method = RequestMethod.PUT) 的简写。
     * HTTP PUT 方法在 REST 中代表"完整替换/更新资源"，特征：</p>
     * <ul>
     *   <li><b>幂等性</b>：多次相同的 PUT 请求，结果一致（重复更新同一个资源，最终状态相同）</li>
     *   <li><b>语义</b>：PUT 要求提供资源的完整表示（全部字段），PATCH 则是部分更新</li>
     *   <li>URL 中通常包含目标资源的 ID</li>
     * </ul>
     *
     * <h2>@PathVariable 与 @RequestBody 的组合使用</h2>
     * <p>在更新场景中，通常同时使用两种参数绑定方式：</p>
     * <ul>
     *   <li><code>@PathVariable</code> — 从 URL 中提取"要更新哪个资源"（资源标识符 id）</li>
     *   <li><code>@RequestBody</code> — 从请求体中提取"要更新成什么内容"（更新的数据）</li>
     * </ul>
     *
     * @param id      URL 路径中的用户 ID，标识要更新的目标资源
     * @param request 包含更新后数据的请求体，经过 @Valid 校验
     * @return 200 OK，响应体包含更新后的用户信息；404 如果用户不存在
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "更新指定 ID 的用户信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "用户更新成功"),
        @ApiResponse(responseCode = "404", description = "用户不存在"),
        @ApiResponse(responseCode = "400", description = "请求参数验证失败")
    })
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody CreateUserRequest request) {
        log.info("更新用户: {}", id);
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    /**
     * DELETE /api/users/{id} — 删除用户。
     *
     * <h2>核心概念：@DeleteMapping — 映射 HTTP DELETE 请求</h2>
     * <p>@DeleteMapping 是 @RequestMapping(method = RequestMethod.DELETE) 的简写。
     * HTTP DELETE 方法在 REST 中代表"删除资源"，特征：</p>
     * <ul>
     *   <li><b>幂等性</b>：多次删除同一资源，最终结果相同（资源都不存在）</li>
     *   <li><b>URL</b>：通过 URL 路径中的 ID 指定要删除的资源</li>
     *   <li><b>无请求体</b>：DELETE 通常不带请求体，所有信息通过 URL 传递</li>
     * </ul>
     *
     * <h2>REST 删除资源的约定：返回 204 No Content</h2>
     * <p>REST 规范建议删除成功后返回 204 No Content（而非 200 OK），表示请求成功处理但响应体为空。
     * 这里 <code>ResponseEntity.noContent().build()</code> 返回 204 状态码且无响应体。</p>
     *
     * <h2>@ApiResponse 中 204 的含义</h2>
     * <p>204 No Content 是 HTTP 规范的一部分：服务器成功处理请求，但不需要返回任何实体内容。
     * 浏览器收到 204 后不会刷新页面，这对 DELETE 操作特别合适。</p>
     *
     * @param id URL 路径中要删除的用户 ID
     * @return 204 No Content（删除成功）；404 Not Found（用户不存在）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "删除指定 ID 的用户")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "用户删除成功"),
        @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        log.info("删除用户: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
