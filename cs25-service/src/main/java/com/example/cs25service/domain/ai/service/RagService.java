package com.example.cs25service.domain.ai.service;

import java.util.List;
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
        vectorStore.add(docs);
        System.out.println(docs.size() + "개 문서 저장 완료");
    }


    public List<Document> searchRelevant(String query, int topK, double similarityThreshold) {
        return vectorStore.similaritySearch(SearchRequest.builder()
            .query(query)
            .topK(topK)
            .similarityThreshold(similarityThreshold)
            .build());
    }
}
