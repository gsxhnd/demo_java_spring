package com.example.security.service;

import com.example.security.dto.AuthResponse;
import com.example.security.dto.LoginRequest;
import com.example.security.dto.RegisterRequest;
import com.example.security.entity.User;
import com.example.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * 用户注册
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已被使用");
        }

        // 设置默认角色
        Set<String> roles = request.getRoles();
        if (roles == null || roles.isEmpty()) {
            roles = Set.of("USER");
        }

        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .roles(roles)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        userRepository.save(user);
        log.info("用户注册成功: {}", user.getUsername());

        // 生成 Token
        return generateAuthResponse(user);
    }

    /**
     * 用户登录
     */
    public AuthResponse login(LoginRequest request) {
        // 验证凭证
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 获取用户信息
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("用户登录成功: {}", user.getUsername());

        // 生成 Token
        return generateAuthResponse(user);
    }

    /**
     * 刷新 Token
     */
    public AuthResponse refreshToken(String refreshToken) {
        // 验证 refresh token 是否有效
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("无效的 Refresh Token");
        }

        // 提取用户名
        String username = jwtService.extractUsername(refreshToken);

        // 获取用户信息
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 验证 token 与用户匹配
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new IllegalArgumentException("Refresh Token 已过期或无效");
        }

        log.info("刷新 Token 成功: {}", username);

        // 生成新的 Access Token
        return generateAuthResponse(user);
    }

    /**
     * 生成认证响应
     */
    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // 这里只返回 accessToken，refreshToken 可以存储在客户端
        // 实际生产中可能需要将 refreshToken 存储到 Redis 或数据库
        return AuthResponse.of(
                accessToken,
                jwtService.getAccessTokenExpiration(),
                user.getUsername(),
                user.getRoles()
        );
    }
}
