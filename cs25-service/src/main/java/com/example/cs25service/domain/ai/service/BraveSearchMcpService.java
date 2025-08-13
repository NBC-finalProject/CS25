package com.example.cs25service.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BraveSearchMcpService {

    private static final String BRAVE_WEB_TOOL = "brave_web_search";

    private final List<McpSyncClient> mcpClients;
    private final ObjectMapper objectMapper;

    public JsonNode search(String query, int count, int offset) {
        McpSyncClient braveClient = resolveBraveClient();

        CallToolRequest request = new CallToolRequest(
            BRAVE_WEB_TOOL,
            Map.of("query", query, "count", count, "offset", offset)
        );

        CallToolResult result = braveClient.callTool(request);

        // 원본 로그 (디버그용)
        JsonNode raw = objectMapper.valueToTree(result.content());
        log.info("[Brave MCP Response Raw content]: {}", raw.toPrettyString());

        // 결과 정규화: results 배열에 {url,title,description}
        ArrayNode results = objectMapper.createArrayNode();

        if (raw != null && raw.isArray()) {
            for (JsonNode item : raw) {
                // MCP content 중 text 타입만 처리
                if ("text".equalsIgnoreCase(item.path("type").asText())) {
                    String text = item.path("text").asText("").trim();
                    if (text.isEmpty()) {
                        continue;
                    }

                    // 1) 우선 JSON으로 파싱 시도 (대부분 여기서 해결)
                    if (looksLikeJson(text)) {
                        parseJsonBlockIntoResults(text, results);
                        continue;
                    }

                    // 2) 혹시 text가 "{"url":...}" 같은 JSON 문자열 리터럴로 들어온 경우
                    //    readTree하면 TextNode가 나오니 한 번 더 벗겨서 재파싱
                    try {
                        JsonNode maybeString = objectMapper.readTree(text);
                        if (maybeString.isTextual() && looksLikeJson(maybeString.asText())) {
                            parseJsonBlockIntoResults(maybeString.asText(), results);
                            continue;
                        }
                    } catch (Exception ignored) {
                        // 그냥 패스하고 fallback로
                    }

                    // 3) Fallback: "Title:..., URL:..., (내용...)" 포맷 처리
                    addFromTitleUrlBlock(text, results);
                }
            }
        }

        ObjectNode root = objectMapper.createObjectNode();
        root.set("results", results);
        log.info("Brave 검색 결과 {}건 추출", results.size());
        return root;
    }

    private McpSyncClient resolveBraveClient() {
        for (McpSyncClient client : mcpClients) {
            ListToolsResult tools;
            try {
                tools = client.listTools();
            } catch (McpError e) {
                // 초기화 안 된 클라이언트 방어
                if (e.getMessage() != null &&
                    e.getMessage().toLowerCase(Locale.ROOT).contains("initialized")) {
                    client.initialize();
                    tools = client.listTools();
                } else {
                    throw e;
                }
            }

            if (tools != null && tools.tools() != null) {
                boolean found = tools.tools().stream()
                    .anyMatch(tool -> BRAVE_WEB_TOOL.equalsIgnoreCase(tool.name()));
                if (found) {
                    return client;
                }
            }
        }
        throw new IllegalStateException("Brave MCP 서버에서 " + BRAVE_WEB_TOOL + " 툴을 찾을 수 없습니다.");
    }

    /* ------------------ helpers ------------------ */

    private boolean looksLikeJson(String s) {
        String t = s.trim();
        return (t.startsWith("{") && t.endsWith("}")) || (t.startsWith("[") && t.endsWith("]"));
    }

    private void parseJsonBlockIntoResults(String json, ArrayNode out) {
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node.isArray()) {
                for (JsonNode obj : node) {
                    addObjToResults(obj, out);
                }
            } else if (node.isObject()) {
                addObjToResults(node, out);
            } else if (node.isTextual() && looksLikeJson(node.asText())) {
                // 이중 문자열화된 JSON 방어
                parseJsonBlockIntoResults(node.asText(), out);
            }
        } catch (Exception e) {
            log.warn("Brave MCP JSON 파싱 실패: {}", truncate(json, 400), e);
        }
    }

    private void addObjToResults(JsonNode obj, ArrayNode out) {
        String url = asStr(obj.get("url"));
        String title = asStr(obj.get("title"));
        String description = asStr(firstNonNull(
            obj.get("description"),
            obj.get("snippet"),
            obj.get("summary"),
            obj.get("content")
        ));

        // 최소 하나는 있어야 추가
        if ((url != null && !url.isBlank()) ||
            (title != null && !title.isBlank()) ||
            (description != null && !description.isBlank())) {
            ObjectNode one = objectMapper.createObjectNode();
            if (url != null) {
                one.put("url", url);
            }
            if (title != null) {
                one.put("title", title);
            }
            if (description != null) {
                one.put("description", description);
            }
            out.add(one);
        }
    }

    private JsonNode firstNonNull(JsonNode... nodes) {
        for (JsonNode n : nodes) {
            if (n != null && !n.isNull()) {
                return n;
            }
        }
        return null;
    }

    private String asStr(JsonNode n) {
        return (n == null || n.isNull()) ? null : n.asText(null);
    }

    private void addFromTitleUrlBlock(String text, ArrayNode out) {
        String[] lines = text.split("\\r?\\n");
        String title = null;
        String url = null;
        StringBuilder body = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith("Title:")) {
                // 이전 블럭 flush
                flushOne(out, title, url, body);
                title = line.replaceFirst("Title:", "").trim();
                url = null;
                body.setLength(0);
            } else if (line.startsWith("URL:")) {
                url = line.replaceFirst("URL:", "").trim();
            } else {
                body.append(line).append('\n');
            }
        }
        flushOne(out, title, url, body);
    }

    private void flushOne(ArrayNode out, String title, String url, StringBuilder body) {
        if (title != null && url != null && body.length() > 0) {
            ObjectNode one = objectMapper.createObjectNode();
            one.put("title", title);
            one.put("url", url);
            one.put("description", body.toString().trim());
            out.add(one);
        }
    }

    private String truncate(String s, int max) {
        if (s == null || s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "...";
    }
}
