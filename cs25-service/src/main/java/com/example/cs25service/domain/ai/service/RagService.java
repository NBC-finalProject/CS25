package com.example.cs25service.domain.ai.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final VectorStore vectorStore;

    public void saveDocumentsToVectorStore(List<Document> docs) {
        List<Document> validDocs = docs.stream()
            .filter(doc -> doc.getText() != null && !doc.getText().trim().isEmpty())
            .collect(Collectors.toList());

        if (validDocs.isEmpty()) {
            log.warn("저장할 유효한 문서가 없습니다.");
            return;
        }

        log.info("임베딩할 문서 개수: {}", validDocs.size());
        for (Document doc : validDocs) {
            log.info("임베딩할 문서 경로: {}, 글자 수: {}", doc.getMetadata().get("path"),
                doc.getText().length());
            log.info("임베딩할 문서 내용(앞 100자): {}",
                doc.getText().substring(0, Math.min(doc.getText().length(), 100)));
        }

        try {
            vectorStore.add(validDocs);
            log.info("{}개 문서 저장 완료", validDocs.size());
        } catch (Exception e) {
            log.error("벡터스토어 저장 실패: {}", e.getMessage());
            throw e;
        }
    }

    public List<Document> getAllDocuments() {
        List<Document> docs = vectorStore.similaritySearch(SearchRequest.builder()
            .query("")
            .topK(100)
            .build());
        log.info("저장된 문서 개수: {}", docs.size());
        docs.forEach(doc -> log.info("문서 ID: {}, 내용: {}", doc.getId(), doc.getText()));
        return docs;
    }

    public List<Document> searchRelevant(String keyword) {
        List<Document> docs = vectorStore.similaritySearch(SearchRequest.builder()
            .query(keyword)
            .topK(3)
            .similarityThreshold(0.5)
            .build());
        log.info("키워드 '{}'로 검색된 문서 개수: {}", keyword, docs.size());
        docs.forEach(doc -> log.info("검색 결과 - 문서 ID: {}, 내용: {}", doc.getId(), doc.getText()));
        return docs;
    }
}
