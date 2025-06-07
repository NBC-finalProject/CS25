package com.example.cs25.domain.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RagService {

    private final VectorStore vectorStore;

    public void saveDocuments(List<String> contents) {
        List<Document> docs = contents.stream()
                .map(content -> new Document(content))
                .toList();
        vectorStore.add(docs);
    }

    public List<Document> searchRelevant(String query) {
        return vectorStore.similaritySearch(query);
    }
}