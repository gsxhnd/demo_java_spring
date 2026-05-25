package com.example.security.service;

import com.example.security.dto.CreateDocumentRequest;
import com.example.security.dto.DocumentResponse;
import com.example.security.entity.Document;
import com.example.security.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;

    public List<DocumentResponse> findAll() {
        return documentRepository.findAll().stream()
                .map(DocumentResponse::fromEntity)
                .toList();
    }

    public DocumentResponse findById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("文档不存在 - id: " + id));
        return DocumentResponse.fromEntity(document);
    }

    @Transactional
    public DocumentResponse create(CreateDocumentRequest request, String ownerUsername) {
        Document document = Document.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .ownerUsername(ownerUsername)
                .createdAt(LocalDateTime.now())
                .build();
        return DocumentResponse.fromEntity(documentRepository.save(document));
    }
}
