package com.example.mvc.service;

import com.example.mvc.dto.UserRequest;
import com.example.mvc.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户服务
 */
@Service
public class UserService {

    private final Map<Long, User> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public UserService() {
        // 初始化测试数据
        save(buildUser(1L, "张三", "zhangsan@example.com", 25, "13800138001"));
        save(buildUser(2L, "李四", "lisi@example.com", 30, "13800138002"));
        save(buildUser(3L, "王五", "wangwu@example.com", 28, "13800138003"));
    }

    private User buildUser(Long id, String name, String email, Integer age, String phone) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .age(age)
                .phone(phone)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }

    public User findById(Long id) {
        User user = storage.get(id);
        if (user == null) {
            throw new UserNotFoundException("用户不存在: " + id);
        }
        return user;
    }

    public User create(UserRequest request) {
        Long id = idGenerator.getAndIncrement();
        User user = User.builder()
                .id(id)
                .name(request.getName())
                .email(request.getEmail())
                .age(request.getAge())
                .phone(request.getPhone())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        storage.put(id, user);
        return user;
    }

    public User update(Long id, UserRequest request) {
        User user = findById(id);
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setAge(request.getAge());
        user.setPhone(request.getPhone());
        user.setUpdateTime(LocalDateTime.now());
        return user;
    }

    public void delete(Long id) {
        if (!storage.containsKey(id)) {
            throw new UserNotFoundException("用户不存在: " + id);
        }
        storage.remove(id);
    }

    private void save(User user) {
        storage.put(user.getId(), user);
    }

    /**
     * 用户不存在异常
     */
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}
