package com.example.cs25service.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BraveSearchRagService {

    public List<Document> toDocuments(Optional<JsonNode> resultsNodeOpt) {
        List<Document> documents = new ArrayList<>();

        resultsNodeOpt.ifPresent(root -> {
            JsonNode results = root.path("results");
            if (results != null && results.isArray()) {
                results.forEach(r -> {
                    // 정규화된 필드 사용
                    String title = r.path("title").asText("");
                    String url = r.path("url").asText("");
                    String description = r.path("description").asText("");

                    // 예비: 만약 위 값이 비었고 text만 온다면(이중포장 누락 케이스), 텍스트를 본문으로 사용
                    if (title.isBlank() && description.isBlank() && r.hasNonNull("text")) {
                        description = r.path("text").asText("");
                    }

                    // 간단한 HTML 제거
                    if (!description.isBlank()) {
                        description = description.replaceAll("<[^>]+>", "");
                    }

                    if (!title.isBlank() || !description.isBlank()) {
                        documents.add(new Document(
                            title.isBlank() ? url : title,
                            description.isBlank() ? (title.isBlank() ? "" : title) : description,
                            Map.of("title", title, "url", url)
                        ));
                    }
                });
            }
        });

        return documents;
    }
}
