package com.example.multidb.service;

import com.example.multidb.dto.mongo.CreateUserLogRequest;
import com.example.multidb.dto.mongo.UserLogResponse;
import com.example.multidb.entity.mongo.UserLog;
import com.example.multidb.exception.ResourceNotFoundException;
import com.example.multidb.repository.mongo.UserLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLogService {

    private final UserLogRepository userLogRepository;

    public List<UserLogResponse> findAll() {
        return userLogRepository.findAll().stream()
                .map(UserLogResponse::fromEntity)
                .toList();
    }

    public UserLogResponse findById(String id) {
        UserLog userLog = userLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("日志不存在 - id: " + id));
        return UserLogResponse.fromEntity(userLog);
    }

    public List<UserLogResponse> findByUserIdAndAction(Long userId, String action) {
        return userLogRepository.findByUserIdAndAction(userId, action).stream()
                .map(UserLogResponse::fromEntity)
                .toList();
    }

    public Page<UserLogResponse> findRecent(Instant since, Pageable pageable) {
        return userLogRepository.findByCreatedAtAfter(since, pageable)
                .map(UserLogResponse::fromEntity);
    }

    public UserLogResponse create(CreateUserLogRequest request) {
        UserLog userLog = UserLog.builder()
                .userId(request.getUserId())
                .action(request.getAction())
                .details(request.getDetails())
                .createdAt(Instant.now())
                .build();
        UserLog saved = userLogRepository.save(userLog);
        log.info("MongoDB 日志写入 - id: {}, userId: {}, action: {}",
                saved.getId(), saved.getUserId(), saved.getAction());
        return UserLogResponse.fromEntity(saved);
    }

    public void delete(String id) {
        if (!userLogRepository.existsById(id)) {
            throw new ResourceNotFoundException("日志不存在 - id: " + id);
        }
        userLogRepository.deleteById(id);
        log.info("MongoDB 日志删除 - id: {}", id);
    }
}
