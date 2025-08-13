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
            if (!results.isArray()) {
                return;
            }

            for (JsonNode r : results) {
                // 1) 구조화된 JSON 우선 처리
                String url = r.path("url").asText(null);
                String title = r.path("title").asText(null);
                String description = r.path("description").asText(null);

                if ((url != null && !url.isBlank()) ||
                    (title != null && !title.isBlank()) ||
                    (description != null && !description.isBlank())) {
                    String docTitle = (title != null && !title.isBlank())
                        ? title : (url != null ? url : "Web result");
                    String body = (description != null) ? description : "";
                    documents.add(new Document(
                        docTitle,
                        body,
                        Map.of("title", docTitle, "url", url == null ? "" : url)
                    ));
                    continue;
                }

                // 2) Fallback: "Title:/URL:" 텍스트 포맷
                String text = r.path("text").asText("");
                if (text.isBlank()) {
                    continue;
                }

                String[] lines = text.split("\\n");
                String curTitle = null;
                String curUrl = null;
                StringBuilder contentBuilder = new StringBuilder();

                for (String line : lines) {
                    if (line.startsWith("Title:")) {
                        if (curTitle != null && curUrl != null && contentBuilder.length() > 0) {
                            documents.add(new Document(
                                curTitle,
                                contentBuilder.toString().trim(),
                                Map.of("title", curTitle, "url", curUrl)
                            ));
                            contentBuilder.setLength(0);
                        }
                        curTitle = line.replaceFirst("Title:", "").trim();
                    } else if (line.startsWith("URL:")) {
                        curUrl = line.replaceFirst("URL:", "").trim();
                    } else {
                        contentBuilder.append(line).append("\n");
                    }
                }

                if (curTitle != null && curUrl != null && contentBuilder.length() > 0) {
                    documents.add(new Document(
                        curTitle,
                        contentBuilder.toString().trim(),
                        Map.of("title", curTitle, "url", curUrl)
                    ));
                }
            }
        });

        return documents;
    }
}
