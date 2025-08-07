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

    private final BraveSearchMcpService braveSearchMcpService;

    public List<Document> toDocuments(Optional<JsonNode> resultsNodeOpt) {
        List<Document> documents = new ArrayList<>();

        resultsNodeOpt.ifPresent(resultsNode -> {
            resultsNode.path("results").forEach(result -> {
                String text = result.path("text").asText("");
                if (text.isBlank()) {
                    return;
                }

                // 여러 문서가 한 개의 텍스트에 포함되어 있으므로 줄 단위로 분리
                String[] lines = text.split("\\n");

                String title = null;
                String url = null;
                StringBuilder contentBuilder = new StringBuilder();

                for (String line : lines) {
                    if (line.startsWith("Title:")) {
                        if (title != null && url != null && contentBuilder.length() > 0) {
                            // 이전 문서를 저장
                            documents.add(new Document(
                                title,
                                contentBuilder.toString().trim(),
                                Map.of("title", title, "url", url)
                            ));
                            contentBuilder.setLength(0);
                        }
                        title = line.replaceFirst("Title:", "").trim();
                    } else if (line.startsWith("URL:")) {
                        url = line.replaceFirst("URL:", "").trim();
                    } else {
                        contentBuilder.append(line).append("\n");
                    }
                }

                // 마지막 문서 저장
                if (title != null && url != null && contentBuilder.length() > 0) {
                    documents.add(new Document(
                        title,
                        contentBuilder.toString().trim(),
                        Map.of("title", title, "url", url)
                    ));
                }
            });
        });

        return documents;
    }

}
