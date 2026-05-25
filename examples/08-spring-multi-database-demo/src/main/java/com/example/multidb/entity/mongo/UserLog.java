package com.example.multidb.entity.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user_logs")
public class UserLog {

    @Id
    private String id;

    private Long userId;

    private String action;

    private Map<String, Object> details;

    private Instant createdAt;
}
