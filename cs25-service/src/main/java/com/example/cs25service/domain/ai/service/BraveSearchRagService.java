package com.example.cs25service.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BraveSearchRagService {

    private final BraveSearchMcpService braveSearchMcpService;

    public List<Document> searchRelevant(String query, int topK,
        double similarityThresholdIgnored) {
        JsonNode resultsNode = braveSearchMcpService.search(query, topK, 0);

        List<Document> documents = new ArrayList<>();

        resultsNode.path("results").forEach(result -> {
            String title = result.path("title").asText("");
            String url = result.path("url").asText("");
            String content = result.path("content").asText(""); // 내용 일부

            if (!content.isBlank()) {
                Document doc = new Document(
                    title,
                    content,
                    Map.of("title", title, "url", url)
                );
                documents.add(doc);
            }
        });

        return documents;
    }
}
