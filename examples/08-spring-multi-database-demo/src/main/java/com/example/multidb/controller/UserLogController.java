package com.example.multidb.controller;

import com.example.multidb.dto.mongo.CreateUserLogRequest;
import com.example.multidb.dto.mongo.UserLogResponse;
import com.example.multidb.service.UserLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/mongo/logs")
@RequiredArgsConstructor
@Tag(name = "MongoDB", description = "MongoDB 用户行为日志")
public class UserLogController {

    private final UserLogService userLogService;

    @GetMapping
    @Operation(summary = "获取所有日志")
    public ResponseEntity<List<UserLogResponse>> getAll() {
        return ResponseEntity.ok(userLogService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据 ID 获取日志")
    public ResponseEntity<UserLogResponse> getById(
            @Parameter(description = "日志 ID") @PathVariable String id) {
        return ResponseEntity.ok(userLogService.findById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "按用户 ID 与行为查询")
    public ResponseEntity<List<UserLogResponse>> search(
            @RequestParam Long userId,
            @RequestParam String action) {
        return ResponseEntity.ok(userLogService.findByUserIdAndAction(userId, action));
    }

    @GetMapping("/recent")
    @Operation(summary = "分页查询最近日志")
    public ResponseEntity<Page<UserLogResponse>> recent(
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(userLogService.findRecent(since, pageable));
    }

    @PostMapping
    @Operation(summary = "创建用户行为日志")
    public ResponseEntity<UserLogResponse> create(@Valid @RequestBody CreateUserLogRequest request) {
        return new ResponseEntity<>(userLogService.create(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除日志")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        userLogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
