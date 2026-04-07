package com.example.security.config;

import com.example.security.entity.User;
import com.example.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * 数据初始化配置
 * 在应用启动时创建演示用户
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // 创建普通用户
            if (!userRepository.existsByUsername("user")) {
                User regularUser = User.builder()
                        .username("user")
                        .password(passwordEncoder.encode("user123"))
                        .email("user@example.com")
                        .roles(Set.of("USER"))
                        .enabled(true)
                        .accountNonLocked(true)
                        .build();
                userRepository.save(regularUser);
                log.info("创建演示用户: user / user123 (ROLE: USER)");
            }

            // 创建管理员用户
            if (!userRepository.existsByUsername("admin")) {
                User adminUser = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .email("admin@example.com")
                        .roles(Set.of("USER", "ADMIN"))
                        .enabled(true)
                        .accountNonLocked(true)
                        .build();
                userRepository.save(adminUser);
                log.info("创建演示管理员: admin / admin123 (ROLE: USER, ADMIN)");
            }

            log.info("数据初始化完成，当前用户数量: {}", userRepository.count());
        };
    }
}
