package com.example.testing.slice;

import com.example.testing.entity.User;
import com.example.testing.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("slice")
@DataJpaTest
@DisplayName("UserRepository 切片测试（@DataJpaTest + TestEntityManager）")
class UserRepositoryDataJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("应按邮箱查询用户")
    void shouldFindByEmail() {
        User user = User.builder()
                .username("alice")
                .email("alice@example.com")
                .age(25)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByEmail("alice@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("alice");
    }

    @Test
    @DisplayName("existsByUsername 应正确判断")
    void shouldDetectExistingUsername() {
        entityManager.persistAndFlush(User.builder()
                .username("bob")
                .email("bob@example.com")
                .age(30)
                .createdAt(LocalDateTime.now())
                .build());

        assertThat(userRepository.existsByUsername("bob")).isTrue();
        assertThat(userRepository.existsByUsername("unknown")).isFalse();
    }

    @Test
    @DisplayName("应持久化并生成 ID")
    void shouldPersistUser() {
        User saved = userRepository.save(User.builder()
                .username("carol")
                .email("carol@example.com")
                .age(28)
                .createdAt(LocalDateTime.now())
                .build());

        assertThat(saved.getId()).isNotNull();
        assertThat(userRepository.findById(saved.getId())).isPresent();
    }
}
