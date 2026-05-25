package com.example.testing.slice;

import com.example.testing.controller.UserController;
import com.example.testing.dto.CreateUserRequest;
import com.example.testing.dto.UserResponse;
import com.example.testing.exception.UserNotFoundException;
import com.example.testing.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("slice")
@WebMvcTest(controllers = UserController.class)
@DisplayName("UserController 切片测试（@WebMvcTest + MockMvc）")
class UserControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("参数校验失败应返回 400")
    void shouldReturn400_whenValidationFails() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"ab","email":"invalid","age":10}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("请求参数验证失败"));
    }

    @Test
    @DisplayName("用户存在时应返回 200 与 snake_case JSON 字段")
    void shouldReturnUser_whenExists() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .age(25)
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
        when(userService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_name").value("alice"))
                .andExpect(jsonPath("$.user_email").value("alice@example.com"));
    }

    @Test
    @DisplayName("用户不存在时应返回 404")
    void shouldReturn404_whenUserNotFound() throws Exception {
        when(userService.findById(eq(99L)))
                .thenThrow(new UserNotFoundException("用户不存在 - id: 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("用户不存在 - id: 99"));
    }

    @Test
    @DisplayName("创建用户成功应返回 201")
    void shouldCreateUser_whenValidRequest() throws Exception {
        UserResponse created = UserResponse.builder()
                .id(2L)
                .username("bob")
                .email("bob@example.com")
                .age(30)
                .createdAt(LocalDateTime.now())
                .build();
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"bob","email":"bob@example.com","age":30}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user_name").value("bob"));
    }

    @Test
    @DisplayName("列表接口应返回 200")
    void shouldReturnList() throws Exception {
        when(userService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
