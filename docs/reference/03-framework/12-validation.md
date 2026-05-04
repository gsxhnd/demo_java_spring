# Bean Validation — 数据校验 / Data Validation

> @Valid, @Validated, 自定义校验器, 分组校验, 全局异常处理

## 1. 概述 / Overview

Bean Validation（JSR 380）是 Java 标准的数据校验规范，Spring Boot 通过 `spring-boot-starter-validation` 提供开箱即用的支持。它允许通过注解声明式地定义校验规则，避免在业务代码中编写大量 if-else 校验逻辑。

### 核心组件

| 组件 | 说明 |
|---|---|
| Hibernate Validator | Bean Validation 参考实现（Spring Boot 默认集成） |
| `@Valid` | JSR 标准注解，触发级联校验 |
| `@Validated` | Spring 扩展注解，支持分组校验 |
| `ConstraintValidator` | 自定义校验器接口 |
| `MethodValidationPostProcessor` | 方法级校验支持 |

---

## 2. 核心概念 / Core Concepts

### @Valid vs @Validated

| 特性 | `@Valid` (JSR) | `@Validated` (Spring) |
|---|---|---|
| 来源 | `jakarta.validation` | `org.springframework.validation.annotation` |
| 分组校验 | 不支持 | 支持 `@Validated(Create.class)` |
| 嵌套校验 | 支持（标注在字段上） | 不支持嵌套触发 |
| 方法级校验 | 不支持 | 支持（标注在类上） |
| 使用位置 | 参数、字段、返回值 | 类、方法参数 |

### 校验流程

```
HTTP Request
     │
     ▼
┌─────────────────────────────────┐
│  @RestController                │
│                                 │
│  @PostMapping("/users")         │
│  public User create(            │
│      @Valid @RequestBody        │──→ 触发校验
│      UserCreateDTO dto          │
│  )                              │
└─────────────────────────────────┘
     │
     ▼  校验失败
┌─────────────────────────────────┐
│  MethodArgumentNotValidException│
│                                 │
│  → @ControllerAdvice 捕获       │
│  → 返回统一错误响应              │
└─────────────────────────────────┘
```

### 内置约束注解一览

| 注解 | 说明 | 示例 |
|---|---|---|
| `@NotNull` | 不能为 null | `@NotNull String name` |
| `@NotBlank` | 不能为 null 且去空格后长度 > 0 | `@NotBlank String name` |
| `@NotEmpty` | 不能为 null 且不能为空（String/Collection/Map/Array） | `@NotEmpty List<String> tags` |
| `@Size` | 长度/大小范围 | `@Size(min=2, max=50) String name` |
| `@Min` / `@Max` | 数值最小/最大值 | `@Min(0) @Max(150) Integer age` |
| `@Email` | 邮箱格式 | `@Email String email` |
| `@Pattern` | 正则匹配 | `@Pattern(regexp="^1[3-9]\\d{9}$") String phone` |
| `@Past` / `@Future` | 过去/未来的日期 | `@Past LocalDate birthday` |
| `@Positive` / `@Negative` | 正数/负数 | `@Positive BigDecimal price` |
| `@DecimalMin` / `@DecimalMax` | 精确数值范围 | `@DecimalMin("0.01") BigDecimal amount` |

---

## 3. 快速开始 / Quick Start

### 3.1 添加依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### 3.2 定义 DTO 与校验规则

```java
public class UserCreateDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度 2~50")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "年龄不能为空")
    @Min(value = 1, message = "年龄最小为 1")
    @Max(value = 150, message = "年龄最大为 150")
    private Integer age;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    // getters & setters
}
```

### 3.3 Controller 中使用 @Valid

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody UserCreateDTO dto) {
        // 校验通过才会执行到这里
        User user = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
```

### 3.4 全局校验异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 @RequestBody 校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = Map.of(
            "code", 400,
            "message", "参数校验失败",
            "errors", fieldErrors
        );
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * 处理 @RequestParam / @PathVariable 校验失败
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex) {

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v ->
            errors.put(v.getPropertyPath().toString(), v.getMessage())
        );

        Map<String, Object> body = Map.of(
            "code", 400,
            "message", "参数校验失败",
            "errors", errors
        );
        return ResponseEntity.badRequest().body(body);
    }
}
```

响应示例：

```json
{
  "code": 400,
  "message": "参数校验失败",
  "errors": {
    "username": "用户名不能为空",
    "email": "邮箱格式不正确"
  }
}
```

---

## 4. 进阶用法 / Advanced Usage

### 4.1 分组校验 / Validation Groups

不同场景（创建 vs 更新）使用不同的校验规则：

```java
// 定义分组接口
public interface Create {}
public interface Update {}

public class UserDTO {

    @Null(groups = Create.class, message = "创建时不能指定 ID")
    @NotNull(groups = Update.class, message = "更新时必须指定 ID")
    private Long id;

    @NotBlank(groups = {Create.class, Update.class}, message = "用户名不能为空")
    private String username;

    @NotBlank(groups = Create.class, message = "创建时密码不能为空")
    private String password;
}
```

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public ResponseEntity<User> create(
            @Validated(Create.class) @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(
            @Validated(Update.class) @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.update(dto));
    }
}
```

### 4.2 嵌套对象校验 / Nested Validation

```java
public class OrderCreateDTO {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @Valid  // 必须加 @Valid 才能触发嵌套校验
    @NotNull(message = "收货地址不能为空")
    private AddressDTO address;

    @Valid
    @NotEmpty(message = "订单项不能为空")
    private List<OrderItemDTO> items;
}

public class AddressDTO {

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "详细地址不能为空")
    private String detail;
}
```

### 4.3 自定义校验注解 / Custom Constraint

```java
// 1. 定义注解
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = PhoneValidator.class)
public @interface Phone {
    String message() default "手机号格式不正确";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// 2. 实现校验器
public class PhoneValidator implements ConstraintValidator<Phone, String> {

    private static final Pattern PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;  // null 由 @NotBlank 处理
        }
        return PATTERN.matcher(value).matches();
    }
}

// 3. 使用
public class UserCreateDTO {
    @NotBlank
    @Phone
    private String phone;
}
```

### 4.4 方法级校验 / Method-Level Validation

对 `@RequestParam`、`@PathVariable` 等非 `@RequestBody` 参数进行校验：

```java
@Validated  // 必须在类上标注
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(
            @PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<User>> search(
            @RequestParam @NotBlank(message = "关键词不能为空") String keyword,
            @RequestParam @Min(1) @Max(100) Integer size) {
        return ResponseEntity.ok(userService.search(keyword, size));
    }
}
```

### 4.5 校验顺序 / Validation Order

使用 `@GroupSequence` 控制校验顺序，前一组失败则不执行后续组：

```java
public interface BasicCheck {}
public interface AdvancedCheck {}

@GroupSequence({BasicCheck.class, AdvancedCheck.class})
public interface OrderedChecks {}

public class UserDTO {
    @NotBlank(groups = BasicCheck.class)
    private String username;

    @Email(groups = AdvancedCheck.class)
    private String email;
}
```

---

## 5. 常见问题 / FAQ

### Q1: @Valid 和 @Validated 到底用哪个？

- 简单场景（无分组）：两者都可以，`@Valid` 更通用
- 需要分组校验：必须用 `@Validated(Group.class)`
- 方法级校验（@RequestParam 等）：必须在类上标注 `@Validated`
- 嵌套对象校验：字段上必须用 `@Valid`

### Q2: 校验不生效？

常见原因：
1. 缺少 `spring-boot-starter-validation` 依赖
2. `@RequestBody` 参数忘记加 `@Valid` / `@Validated`
3. 方法级校验忘记在类上加 `@Validated`
4. 嵌套对象字段忘记加 `@Valid`
5. 自定义校验器未被 Spring 管理（需要 `@Component` 或无参构造）

### Q3: @NotNull、@NotBlank、@NotEmpty 的区别？

| 注解 | null | `""` | `"  "` | 适用类型 |
|---|---|---|---|---|
| `@NotNull` | 不通过 | 通过 | 通过 | 所有类型 |
| `@NotEmpty` | 不通过 | 不通过 | 通过 | String, Collection, Map, Array |
| `@NotBlank` | 不通过 | 不通过 | 不通过 | 仅 String |

### Q4: 如何国际化校验消息？

在 `src/main/resources/` 下创建 `ValidationMessages.properties`：

```properties
# ValidationMessages.properties (默认)
user.name.notblank=Username is required

# ValidationMessages_zh_CN.properties
user.name.notblank=用户名不能为空
```

```java
@NotBlank(message = "{user.name.notblank}")
private String username;
```

### Q5: 如何在 Service 层手动触发校验？

```java
@Service
public class UserService {

    private final Validator validator;

    public UserService(Validator validator) {
        this.validator = validator;
    }

    public void process(UserDTO dto) {
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        // 业务逻辑
    }
}
```

---

## 6. 示例项目 / Example

- 示例项目 → [`examples/spring-validation-demo/`](../../examples/spring-validation-demo/)（待创建）

---

## 7. 参考资料 / References

- [Jakarta Bean Validation 3.0 Spec](https://jakarta.ee/specifications/bean-validation/3.0/)
- [Hibernate Validator 文档](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/)
- [Spring Validation 官方文档](https://docs.spring.io/spring-framework/reference/core/validation.html)
