package com.example.datajpa.service;

import com.example.datajpa.dto.CreateUserRequest;
import com.example.datajpa.dto.UserResponse;
import com.example.datajpa.entity.Address;
import com.example.datajpa.entity.User;
import com.example.datajpa.exception.UserNotFoundException;
import com.example.datajpa.repository.read.UserReadRepository;
import com.example.datajpa.repository.write.UserWriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserWriteRepository writeRepository;
    private final UserReadRepository readRepository;

    public List<UserResponse> getAllUsers() {
        log.info("查询所有用户");
        return readRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public Page<UserResponse> getUsersByPage(Pageable pageable) {
        log.info("分页查询用户 - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return readRepository.findAll(pageable).map(UserResponse::fromEntity);
    }

    public UserResponse getUserById(Long id) {
        log.info("查询用户 - id: {}", id);
        User user = readRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("用户不存在 - id: " + id));
        return UserResponse.fromEntity(user);
    }

    public List<UserResponse> searchUsers(String keyword) {
        log.info("搜索用户 - keyword: {}", keyword);
        return readRepository.searchByKeyword(keyword).stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public List<UserResponse> getUsersByCity(String city) {
        log.info("按城市查询用户 - city: {}", city);
        return readRepository.findByCity(city).stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public List<UserResponse> getActiveUsers(Pageable pageable) {
        log.info("查询活跃用户");
        return readRepository.findByStatus(User.UserStatus.ACTIVE, pageable).stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("创建用户 - username: {}, email: {}", request.getUsername(), request.getEmail());

        if (readRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在: " + request.getUsername());
        }
        if (readRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在: " + request.getEmail());
        }

        Address address = Address.builder()
                .province(request.getProvince())
                .city(request.getCity())
                .street(request.getStreet())
                .zipCode(request.getZipCode())
                .build();

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .age(request.getAge())
                .status(User.UserStatus.ACTIVE)
                .address(address)
                .build();

        User saved = writeRepository.save(user);
        log.info("用户创建成功 - id: {}", saved.getId());
        return UserResponse.fromEntity(saved);
    }

    @Transactional
    public UserResponse updateUser(Long id, CreateUserRequest request) {
        log.info("更新用户 - id: {}", id);

        User user = writeRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("用户不存在 - id: " + id));

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setAge(request.getAge());

        if (request.getProvince() != null || request.getCity() != null) {
            Address address = user.getAddress() != null ? user.getAddress() : new Address();
            if (request.getProvince() != null) address.setProvince(request.getProvince());
            if (request.getCity() != null) address.setCity(request.getCity());
            if (request.getStreet() != null) address.setStreet(request.getStreet());
            if (request.getZipCode() != null) address.setZipCode(request.getZipCode());
            user.setAddress(address);
        }

        User saved = writeRepository.save(user);
        log.info("用户更新成功 - id: {}", saved.getId());
        return UserResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("删除用户 - id: {}", id);

        User user = writeRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("用户不存在 - id: " + id));

        user.setStatus(User.UserStatus.DELETED);
        writeRepository.save(user);
        log.info("用户已标记为删除 - id: {}", id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public UserResponse createUserInNewTransaction(CreateUserRequest request) {
        log.info("在新事务中创建用户 (REQUIRES_NEW + READ_COMMITTED)");
        return createUser(request);
    }
}
