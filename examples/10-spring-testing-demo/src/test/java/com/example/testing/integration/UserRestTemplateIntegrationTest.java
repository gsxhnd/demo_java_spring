package com.example.testing.integration;

import com.example.testing.dto.CreateUserRequest;
import com.example.testing.dto.UserResponse;
import com.example.testing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
@DisplayName("用户 API 集成测试（@SpringBootTest + TestRestTemplate）")
class UserRestTemplateIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("TestRestTemplate 应完成创建与查询")
    void shouldCreateAndGetViaRestTemplate() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("rest_user")
                .email("rest@example.com")
                .age(35)
                .build();

        ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
                "/api/users", request, UserResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getUsername()).isEqualTo("rest_user");

        Long id = createResponse.getBody().getId();
        ResponseEntity<UserResponse> getResponse = restTemplate.exchange(
                "/api/users/" + id,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                UserResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getEmail()).isEqualTo("rest@example.com");
    }

    @Test
    @DisplayName("查询不存在的用户应返回 404")
    void shouldReturn404_whenNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/users/99999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
