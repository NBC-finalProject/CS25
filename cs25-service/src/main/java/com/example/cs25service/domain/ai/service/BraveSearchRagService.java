package com.example.cs25service.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Document> toDocuments(Optional<JsonNode> resultsNodeOpt) {
        List<Document> documents = new ArrayList<>();
        resultsNodeOpt.ifPresent(root -> {
            for (JsonNode r : root.path("results")) {
                // 1) 표준 필드 우선 사용
                String url = getText(r, "url");
                String title = getText(r, "title");
                String description = getText(r, "description");

                // 2) text 필드가 JSON 문자열일 수 있음
                if (isBlank(url) && isBlank(title) && isBlank(description)) {
                    String text = getText(r, "text");
                    if (!isBlank(text) && looksLikeJson(text)) {
                        try {
                            JsonNode inner = objectMapper.readTree(text);
                            url = isBlank(url) ? getText(inner, "url") : url;
                            title = isBlank(title) ? getText(inner, "title") : title;
                            description =
                                isBlank(description) ? getText(inner, "description") : description;
                        } catch (Exception ignored) { /* fall back below */ }
                    }
                }

                // 3) 레거시 포맷: "Title:/URL:" 라인 파싱
                if (isBlank(url) && isBlank(title) && isBlank(description)) {
                    String text = getText(r, "text");
                    if (!isBlank(text)) {
                        ParsedLegacy pl = parseLegacyBlock(text);
                        if (pl != null) {
                            url = pl.url != null ? pl.url : url;
                            title = pl.title != null ? pl.title : title;
                            description = pl.body != null ? pl.body : description;
                        }
                    }
                }

                // 4) 아무 것도 없으면 스킵
                if (isBlank(url) && isBlank(title) && isBlank(description)) {
                    continue;
                }

                // 5) Document id는 URL > title 우선
                String id = !isBlank(url) ? url : title;
                String body = !isBlank(description) ? description
                    : (!isBlank(title) ? title : (url != null ? url : ""));

                documents.add(new Document(
                    id,
                    body,
                    Map.of(
                        "title", title == null ? "" : title,
                        "url", url == null ? "" : url
                    )
                ));
            }
        });
        return documents;
    }

    /* ----------------- helpers ----------------- */

    private String getText(JsonNode n, String field) {
        return (n != null && n.has(field) && n.get(field).isTextual())
            ? n.get(field).asText().trim() : null;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private boolean looksLikeJson(String s) {
        String t = s.trim();
        return (t.startsWith("{") && t.endsWith("}")) ||
            (t.startsWith("[") && t.endsWith("]")) ||
            t.contains("\"url\":") || t.contains("\"title\":") || t.contains("\"description\":");
    }

    /**
     * "Title: ..." / "URL: ..." 블록을 관대한 방식으로 파싱
     */
    private ParsedLegacy parseLegacyBlock(String text) {
        if (text == null) {
            return null;
        }
        String[] lines = text.split("\\r?\\n");
        String title = null, url = null;
        StringBuilder body = new StringBuilder();

        // "Title:" / "URL:" 키워드는 대소문자 무시 + 앞뒤 공백 관대 처리
        for (String raw : lines) {
            String line = raw == null ? "" : raw.trim();
            if (line.regionMatches(true, 0, "Title:", 0, 6)) {
                // 이전 누적 flush
                // (여기서는 단일 레코드만 기대하므로 flush 없이 교체)
                title = line.substring(6).trim();
            } else if (line.regionMatches(true, 0, "URL:", 0, 4)) {
                url = line.substring(4).trim();
            } else {
                body.append(line).append('\n');
            }
        }
        String desc = body.toString().trim();
        if (isBlank(title) && isBlank(url) && isBlank(desc)) {
            return null;
        }
        return new ParsedLegacy(title, url, desc);
    }

    private static class ParsedLegacy {

        final String title;
        final String url;
        final String body;

        ParsedLegacy(String title, String url, String body) {
            this.title = title;
            this.url = url;
            this.body = body;
        }
    }
}
