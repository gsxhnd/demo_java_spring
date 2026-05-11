package com.example.mybatis.service;

import com.example.mybatis.dto.CreateUserRequest;
import com.example.mybatis.dto.UserResponse;
import com.example.mybatis.entity.User;
import com.example.mybatis.exception.UserNotFoundException;
import com.example.mybatis.mapper.read.UserReadMapper;
import com.example.mybatis.mapper.write.UserWriteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserWriteMapper writeMapper;
    private final UserReadMapper readMapper;

    public List<UserResponse> getAllUsers() {
        log.info("查询所有用户");
        return readMapper.findAll().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        log.info("查询用户 - id: {}", id);
        User user = readMapper.findById(id)
                .orElseThrow(() -> new UserNotFoundException("用户不存在 - id: " + id));
        return UserResponse.fromEntity(user);
    }

    public List<UserResponse> searchUsers(String keyword) {
        log.info("搜索用户 - keyword: {}", keyword);
        return readMapper.searchByKeyword(keyword).stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public List<UserResponse> getUsersByCity(String city) {
        log.info("按城市查询用户 - city: {}", city);
        return readMapper.findByCity(city).stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public List<UserResponse> getUsersByStatus(String status) {
        log.info("按状态查询用户 - status: {}", status);
        return readMapper.findByStatus(status).stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public List<UserResponse> getUsersByDynamicConditions(
            String username, String email, String status,
            Integer minAge, Integer maxAge, String city, String orderBy) {
        log.info("动态条件查询用户");
        return readMapper.findWithDynamicConditions(username, email, status, minAge, maxAge, city, orderBy)
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public List<UserResponse> getUsersByIds(List<Long> ids) {
        log.info("按ID列表批量查询 - ids: {}", ids);
        return readMapper.findByIds(ids).stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("创建用户 - username: {}, email: {}", request.getUsername(), request.getEmail());

        if (readMapper.countByUsername(request.getUsername()) > 0) {
            throw new IllegalArgumentException("用户名已存在: " + request.getUsername());
        }
        if (readMapper.countByEmail(request.getEmail()) > 0) {
            throw new IllegalArgumentException("邮箱已存在: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .age(request.getAge())
                .status("ACTIVE")
                .province(request.getProvince())
                .city(request.getCity())
                .street(request.getStreet())
                .zipCode(request.getZipCode())
                .build();

        writeMapper.insert(user);
        log.info("用户创建成功 - id: {}", user.getId());
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, CreateUserRequest request) {
        log.info("更新用户 - id: {}", id);

        User existing = readMapper.findById(id)
                .orElseThrow(() -> new UserNotFoundException("用户不存在 - id: " + id));

        existing.setUsername(request.getUsername());
        existing.setEmail(request.getEmail());
        existing.setAge(request.getAge());
        if (request.getProvince() != null) existing.setProvince(request.getProvince());
        if (request.getCity() != null) existing.setCity(request.getCity());
        if (request.getStreet() != null) existing.setStreet(request.getStreet());
        if (request.getZipCode() != null) existing.setZipCode(request.getZipCode());

        writeMapper.update(existing);
        log.info("用户更新成功 - id: {}", id);
        return UserResponse.fromEntity(existing);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("软删除用户 - id: {}", id);

        User existing = readMapper.findById(id)
                .orElseThrow(() -> new UserNotFoundException("用户不存在 - id: " + id));

        writeMapper.updateStatus(id, "DELETED");
        log.info("用户已标记为删除 - id: {}", id);
    }
}
