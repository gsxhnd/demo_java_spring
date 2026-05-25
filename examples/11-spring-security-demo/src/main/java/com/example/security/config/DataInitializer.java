package com.example.security.config;

import com.example.security.entity.AppUser;
import com.example.security.entity.Document;
import com.example.security.repository.AppUserRepository;
import com.example.security.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final AppUserRepository appUserRepository;
    private final DocumentRepository documentRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedData() {
        return args -> {
            if (appUserRepository.count() > 0) {
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            appUserRepository.save(AppUser.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role("admin")
                    .createdAt(now)
                    .build());
            appUserRepository.save(AppUser.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user123"))
                    .role("user")
                    .createdAt(now)
                    .build());
            documentRepository.save(Document.builder()
                    .title("公开文档示例")
                    .content("所有已登录用户可读")
                    .ownerUsername("admin")
                    .createdAt(now)
                    .build());
            log.info("已初始化演示用户 admin/admin123, user/user123");
        };
    }
}
