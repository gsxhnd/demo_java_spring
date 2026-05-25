package com.example.multidb.dto.redis;

import com.example.multidb.entity.redis.CachedUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Redis 缓存用户响应")
public class CacheUserResponse {

    private String key;
    private String username;
    private String email;
    private Integer age;
    private Long ttlSeconds;

    public static CacheUserResponse of(String key, CachedUser user, Long ttlSeconds) {
        return CacheUserResponse.builder()
                .key(key)
                .username(user.getUsername())
                .email(user.getEmail())
                .age(user.getAge())
                .ttlSeconds(ttlSeconds)
                .build();
    }
}
