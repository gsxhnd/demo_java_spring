package com.example.mysqldemo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mysqldemo.entity.User;
import com.example.mysqldemo.mapper.UserMapper;
import com.example.mysqldemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // --- JPA-based operations ---

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User create(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, User updated) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setUsername(updated.getUsername());
        user.setEmail(updated.getEmail());
        user.setAge(updated.getAge());
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    // --- MyBatis-Plus-based operations ---

    public List<User> findByAgeBetween(Integer minAge, Integer maxAge) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(User::getAge, minAge, maxAge);
        return userMapper.selectList(wrapper);
    }

    public List<User> findByEmailDomain(String domain) {
        return userRepository.findByEmailDomain(domain);
    }
}
