package com.example.testing.slice;

import com.example.testing.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("slice")
@JsonTest
@DisplayName("UserResponse JSON 切片测试（@JsonTest + JacksonTester）")
class UserResponseJsonTest {

    @Autowired
    private JacksonTester<UserResponse> json;

    @Test
    @DisplayName("序列化应使用 snake_case 字段名")
    void shouldSerializeToSnakeCase() throws Exception {
        UserResponse user = UserResponse.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .age(25)
                .createdAt(LocalDateTime.of(2026, 5, 25, 10, 30))
                .build();

        assertThat(json.write(user))
                .hasJsonPathNumberValue("$.id", 1)
                .hasJsonPathStringValue("$.user_name", "alice")
                .hasJsonPathStringValue("$.user_email", "alice@example.com")
                .hasJsonPathNumberValue("$.age", 25)
                .hasJsonPathStringValue("$.created_at");
    }

    @Test
    @DisplayName("反序列化应正确映射 snake_case 字段")
    void shouldDeserializeFromSnakeCase() throws Exception {
        String content = """
                {
                  "id": 2,
                  "user_name": "bob",
                  "user_email": "bob@example.com",
                  "age": 30
                }
                """;

        UserResponse user = json.parse(content).getObject();

        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.getUsername()).isEqualTo("bob");
        assertThat(user.getEmail()).isEqualTo("bob@example.com");
        assertThat(user.getAge()).isEqualTo(30);
    }
}
