package com.example.datajpa.controller;

import com.example.datajpa.dto.CreateUserRequest;
import com.example.datajpa.dto.UserResponse;
import com.example.datajpa.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户 CRUD API（Spring Data JPA）")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "分页获取所有用户")
    @ApiResponse(responseCode = "200", description = "成功获取用户列表")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(userService.getUsersByPage(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功获取用户"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索用户（用户名或邮箱模糊匹配）")
    public ResponseEntity<List<UserResponse>> searchUsers(
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        return ResponseEntity.ok(userService.searchUsers(keyword));
    }

    @GetMapping("/by-city")
    @Operation(summary = "按城市查询用户")
    public ResponseEntity<List<UserResponse>> getUsersByCity(
            @Parameter(description = "城市") @RequestParam String city) {
        return ResponseEntity.ok(userService.getUsersByCity(city));
    }

    @GetMapping("/active")
    @Operation(summary = "获取活跃用户分页列表")
    public ResponseEntity<List<UserResponse>> getActiveUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.getActiveUsers(pageable));
    }

    @PostMapping
    @Operation(summary = "创建用户")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "用户创建成功"),
            @ApiResponse(responseCode = "400", description = "请求参数验证失败")
    })
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userService.createUser(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "用户更新成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "400", description = "请求参数验证失败")
    })
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "软删除用户（标记为DELETED状态）")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "用户删除成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
