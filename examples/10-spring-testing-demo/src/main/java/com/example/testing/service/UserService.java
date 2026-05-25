package com.example.testing.service;

import com.example.testing.dto.CreateUserRequest;
import com.example.testing.dto.UserResponse;
import com.example.testing.entity.User;
import com.example.testing.exception.DuplicateUserException;
import com.example.testing.exception.UserNotFoundException;
import com.example.testing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("用户不存在 - id: " + id));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("用户名已存在: " + request.getUsername());
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateUserException("邮箱已存在: " + request.getEmail());
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .age(request.getAge())
                .createdAt(LocalDateTime.now())
                .build();
        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }
}
