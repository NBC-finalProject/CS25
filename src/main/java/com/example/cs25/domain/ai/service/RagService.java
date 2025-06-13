package com.example.cs25.domain.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final VectorStore vectorStore;

    public void saveDocumentsToVectorStore(List<Document> docs) {
        vectorStore.add(docs);
        System.out.println(docs.size() + "개 문서 저장 완료");
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

    public List<Document> searchRelevant(String query, int topK, double similarityThreshold) {
        return vectorStore.similaritySearch(SearchRequest.builder()
            .query(query)
            .topK(topK)
            .similarityThreshold(similarityThreshold)
            .build());
    }
}
