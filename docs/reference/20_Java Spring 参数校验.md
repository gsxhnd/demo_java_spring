---
title: Java Spring 参数校验
created: 2026-05-05 12:16:00
category: Java-Spring
tags:
  - Java
  - Spring
  - Bean-Validation
  - JSR-380
  - Validation
---

<!-- markdownlint-disable MD025 -->

# Java Spring 参数校验

## 为什么要学参数校验

上一节讲了全局异常处理 -- 当异常发生时，如何统一转换为错误响应。但更好的做法是：**在异常发生之前就拦截非法输入。**

客户端传来的数据不可信 -- 可能缺少必填字段、字符串超长、邮箱格式错误、数字超出范围。如果这些非法数据一路穿透到 Service 甚至数据库层才报错，排查成本高、错误信息不友好。参数校验的目标是：**在 Controller 入口处就验证数据合法性，不合法立即返回明确的错误信息。**

---

## 核心概念

### Bean Validation 是什么

**Bean Validation（JSR 380）是 Java 官方的参数校验规范。** 它定义了一套注解（`@NotNull`、`@Size`、`@Email` 等），用声明式的方式在字段上标注校验规则。Hibernate Validator 是这个规范最流行的实现。

类比：Bean Validation 就像表单的前端校验，但放在了后端。你在字段上贴标签说"这个不能为空"、"那个最多 20 个字符"，框架自动帮你检查，不合格就拒绝。

### 为什么需要 Bean Validation

手动校验意味着在每个 Service 方法开头写一堆 if-else：`if (name == null) throw ...`、`if (name.length() > 20) throw ...`。这些校验逻辑分散在代码各处，容易遗漏、难以维护。Bean Validation 把校验规则集中声明在 DTO 字段上，一目了然，且自动执行。

### 没有 Bean Validation 会怎样

你需要在每个接口手动编写校验逻辑，代码冗长且容易遗漏。不同开发者的校验风格不一致，错误信息格式也不统一。有了 Bean Validation，校验规则和 DTO 定义在一起，Spring 自动触发校验，失败时自动抛出异常（由全局异常处理器统一格式化）。

---

## 概念深入解释

### 常用校验注解

| 注解 | 作用 | 适用类型 |
|------|------|----------|
| `@NotNull` | 不能为 null | 任意对象 |
| `@NotBlank` | 不能为 null 且去空格后长度 > 0 | String |
| `@NotEmpty` | 不能为 null 且不能为空 | String、Collection、Map |
| `@Size(min, max)` | 长度/大小在范围内 | String、Collection |
| `@Min(value)` / `@Max(value)` | 数值最小/最大值 | 数字类型 |
| `@Email` | 邮箱格式 | String |
| `@Pattern(regexp)` | 正则匹配 | String |
| `@Positive` / `@PositiveOrZero` | 正数 / 非负数 | 数字类型 |
| `@Past` / `@Future` | 过去/未来的时间 | 日期类型 |

### 基本使用方式

在 DTO 字段上声明规则，在 Controller 参数上加 `@Valid` 触发校验：

```java
// DTO 定义
public class CreateUserDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在 3-20 之间")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "年龄不能为空")
    @Min(value = 1, message = "年龄必须大于 0")
    @Max(value = 150, message = "年龄不能超过 150")
    private Integer age;
}

// Controller 使用
@PostMapping("/users")
public ApiResponse<UserVO> create(@Valid @RequestBody CreateUserDTO dto) {
    // 如果校验失败，方法不会执行，直接抛出 MethodArgumentNotValidException
}
```

### @Valid vs @Validated

| 特性 | `@Valid` (JSR 380) | `@Validated` (Spring) |
|------|--------------------|-----------------------|
| 来源 | `javax.validation` | `org.springframework` |
| 分组校验 | 不支持 | 支持 |
| 嵌套校验 | 支持（加在嵌套字段上） | 支持 |
| 使用位置 | 方法参数、字段 | 类、方法参数 |

实际使用中：需要分组校验时用 `@Validated`，其他场景用 `@Valid` 即可。

### 分组校验

同一个 DTO 在不同场景下校验规则不同（创建时 `id` 不需要，更新时 `id` 必填）：

```java
public class UserDTO {
    @NotNull(groups = Update.class, message = "更新时 ID 不能为空")
    private Long id;

    @NotBlank(groups = {Create.class, Update.class})
    private String username;

    public interface Create {}
    public interface Update {}
}

// Controller
@PostMapping
public void create(@Validated(UserDTO.Create.class) @RequestBody UserDTO dto) {}

@PutMapping("/{id}")
public void update(@Validated(UserDTO.Update.class) @RequestBody UserDTO dto) {}
```

### 嵌套对象校验

DTO 中包含另一个对象时，需要在字段上加 `@Valid` 才能触发嵌套校验：

```java
public class CreateOrderDTO {
    @NotNull
    @Valid  // 触发 Address 内部字段的校验
    private AddressDTO address;
}

public class AddressDTO {
    @NotBlank
    private String city;
    @NotBlank
    private String street;
}
```

### 校验失败的异常处理

校验失败时 Spring 抛出 `MethodArgumentNotValidException`，需要在全局异常处理器中提取字段错误：

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public ApiResponse<?> handleValidation(MethodArgumentNotValidException e) {
    List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
    List<Map<String, String>> details = fieldErrors.stream()
        .map(err -> Map.of(
            "field", err.getField(),
            "message", err.getDefaultMessage()))
        .toList();
    return ApiResponse.error(400, "参数校验失败", details);
}
```

### 自定义校验注解

当内置注解不够用时，可以自定义：

```java
@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = PhoneValidator.class)
public @interface Phone {
    String message() default "手机号格式不正确";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class PhoneValidator implements ConstraintValidator<Phone, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null) return true; // null 由 @NotNull 处理
        return value.matches("^1[3-9]\\d{9}$");
    }
}
```

### 路径参数和查询参数的校验

`@Valid` 只对 `@RequestBody` 的对象生效。对 `@PathVariable` 和 `@RequestParam` 的校验需要在 Controller 类上加 `@Validated`：

```java
@RestController
@Validated  // 启用方法参数校验
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public UserVO getById(@PathVariable @Positive(message = "ID 必须为正数") Long id) {
        // ...
    }
}
```

此时校验失败抛出的是 `ConstraintViolationException`（不是 `MethodArgumentNotValidException`），需要在全局处理器中额外处理。

---

## 核心要点

1. **校验注解加在 DTO 上，`@Valid` 加在 Controller 参数上。** 两者缺一不可 -- 没有 `@Valid` 触发，注解不会生效。
2. **`@NotNull`、`@NotBlank`、`@NotEmpty` 三者不同。** `@NotNull` 只检查非 null；`@NotBlank` 还要求去空格后有内容（仅 String）；`@NotEmpty` 要求集合/字符串非空。
3. **嵌套对象必须加 `@Valid`。** 否则内层对象的校验注解不会被触发。
4. **路径参数校验需要类级别 `@Validated`。** 并且要额外处理 `ConstraintViolationException`。
5. **自定义校验器中 null 值返回 true。** 让 `@NotNull` 负责 null 检查，自定义校验器只关注格式。这样注解可以组合使用。

---

## 常见误区

- **加了校验注解但忘记加 `@Valid`，校验不生效。** 这是最常见的问题。DTO 字段上有 `@NotBlank`，但 Controller 参数没有 `@Valid`，请求直接通过，字段为 null 也不报错。
- **`@NotNull` 用在 String 上以为能防空字符串。** `@NotNull` 只检查 `!= null`，空字符串 `""` 会通过。String 类型应该用 `@NotBlank`。
- **校验失败返回的错误信息不友好。** 默认的 `message` 是英文且不够具体。应该在每个注解上自定义 `message`，如 `@Size(min=3, max=20, message="用户名长度必须在 3-20 之间")`。
- **在 Service 层用 `@Valid` 期望自动校验。** Spring 的自动校验只在 Controller 层（通过 AOP）生效。Service 层如果需要校验，要么手动调用 `Validator`，要么在 Service 类上加 `@Validated`（需要 Spring AOP 代理）。
- **分组校验时忘记指定 group，导致规则不生效。** 一旦使用了分组，没有指定 `groups` 的注解属于 `Default` 组。如果 `@Validated(Create.class)` 不包含 `Default.class`，那些没指定 group 的注解就不会执行。

---

## 与其他概念的关联

- **前置：** [Java Spring 请求处理](./17_Java%20Spring%20请求处理.md) -- 参数绑定完成后才执行校验。[Java Spring 响应与 DTO](./18_Java%20Spring%20响应与%20DTO.md) -- 校验注解加在 Request DTO 的字段上。[Java Spring 异常处理](./19_Java%20Spring%20异常处理.md) -- 校验失败抛出的异常需要全局处理器捕获和格式化。
- **并行：** [Java Spring Controller](./16_Java%20Spring%20Controller.md) -- 校验在 Controller 方法执行前自动触发。
- **后续：** [Java Spring OpenAPI](./21_Java%20Spring%20OpenAPI.md) -- 校验注解会被 springdoc 读取，自动生成 API 文档中的参数约束描述。
