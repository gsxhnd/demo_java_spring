package com.example.testing.unit;

import com.example.testing.dto.CreateUserRequest;
import com.example.testing.dto.UserResponse;
import com.example.testing.entity.User;
import com.example.testing.exception.DuplicateUserException;
import com.example.testing.exception.UserNotFoundException;
import com.example.testing.repository.UserRepository;
import com.example.testing.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 单元测试（JUnit 5 + Mockito）")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("用户存在时应返回 UserResponse")
    void shouldReturnUser_whenUserExists() {
        User user = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .age(25)
                .createdAt(LocalDateTime.now())
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.findById(1L);

        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("用户不存在时应抛出 UserNotFoundException")
    void shouldThrow_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("用户名重复时应抛出 DuplicateUserException")
    void shouldRejectCreate_whenUsernameExists() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("bob")
                .email("bob@example.com")
                .age(30)
                .build();
        when(userRepository.existsByUsername("bob")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessageContaining("bob");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("创建成功时应持久化用户并返回响应")
    void shouldCreateUser_whenValidRequest() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("carol")
                .email("carol@example.com")
                .age(28)
                .build();
        when(userRepository.existsByUsername("carol")).thenReturn(false);
        when(userRepository.findByEmail("carol@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        UserResponse response = userService.createUser(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getUsername()).isEqualTo("carol");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("carol@example.com");
    }
}
