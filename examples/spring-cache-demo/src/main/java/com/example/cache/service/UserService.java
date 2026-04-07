package com.example.cache.service;

import com.example.cache.entity.User;
import com.example.cache.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户服务 - 演示用户缓存（短期 TTL）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * 查询用户 - 使用用户缓存（5分钟 TTL）
     */
    @Cacheable(value = "users", key = "#id", unless = "#result == null")
    public Optional<User> getUser(Long id) {
        log.info("从数据库查询用户: id={}", id);
        return userRepository.findById(id);
    }

    /**
     * 根据用户名查询 - 演示不同缓存配置
     */
    @Cacheable(value = "users", key = "'username:' + #username", unless = "#result == null")
    public Optional<User> getUserByUsername(String username) {
        log.info("从数据库查询用户: username={}", username);
        return userRepository.findByUsername(username);
    }

    /**
     * 创建用户 - 更新缓存
     */
    @CachePut(value = "users", key = "#result.id")
    @Transactional
    public User createUser(User user) {
        log.info("创建用户: {}", user.getUsername());
        return userRepository.save(user);
    }

    /**
     * 更新用户 - 清除相关缓存
     */
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "users", key = "'username:' + #username")
    })
    @Transactional
    public User updateUser(Long id, String username, String email, String fullName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + id));

        user.setEmail(email);
        user.setFullName(fullName);

        return userRepository.save(user);
    }

    /**
     * 删除用户
     */
    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public void deleteUser(Long id) {
        log.info("删除用户: id={}", id);
        userRepository.deleteById(id);
    }
}
