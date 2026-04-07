package com.example.mvc.controller;

import com.example.mvc.dto.ApiResponse;
import com.example.mvc.dto.UserRequest;
import com.example.mvc.model.User;
import com.example.mvc.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RESTful 用户控制器
 *
 * 演示：
 * - 基本的 RESTful API
 * - @PathVariable、@RequestBody、@RequestParam 的使用
 * - @Valid 触发参数校验
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users - 获取所有用户
     */
    @GetMapping
    public ApiResponse<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ApiResponse.success(users);
    }

    /**
     * GET /api/users/{id} - 获取指定用户
     *
     * @PathVariable 从 URL 路径提取变量
     */
    @GetMapping("/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ApiResponse.success(user);
    }

    /**
     * POST /api/users - 创建用户
     *
     * @RequestBody 从请求体反序列化 JSON
     * @Valid 触发 Bean Validation 校验
     */
    @PostMapping
    public ApiResponse<User> createUser(@Valid @RequestBody UserRequest request) {
        User user = userService.create(request);
        return ApiResponse.success("用户创建成功", user);
    }

    /**
     * PUT /api/users/{id} - 更新用户
     */
    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        User user = userService.update(id, request);
        return ApiResponse.success("用户更新成功", user);
    }

    /**
     * DELETE /api/users/{id} - 删除用户
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.success("用户删除成功", null);
    }

    /**
     * GET /api/users/search - 搜索用户（演示 @RequestParam）
     *
     * @RequestParam 从 Query String 提取参数
     */
    @GetMapping("/search")
    public ApiResponse<List<User>> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false, defaultValue = "0") Integer minAge) {
        List<User> users = userService.findAll();
        List<User> filtered = users.stream()
                .filter(u -> name == null || u.getName().contains(name))
                .filter(u -> u.getAge() >= minAge)
                .toList();
        return ApiResponse.success(filtered);
    }
}
