package com.example.security.dto;

import com.example.security.entity.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {

    private Long id;
    private String title;
    private String content;
    private String ownerUsername;
    private LocalDateTime createdAt;

    public static DocumentResponse fromEntity(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .ownerUsername(document.getOwnerUsername())
                .createdAt(document.getCreatedAt())
                .build();
    }
}
