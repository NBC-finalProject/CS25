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
    private static final int MAX_JSON_RECURSION_DEPTH = 3;

    private final List<McpSyncClient> mcpClients;
    private final ObjectMapper objectMapper;

    private static boolean looksLikeJson(String s) {
        if (s == null) {
            return false;
        }
        String t = s.trim();
        return (!t.isEmpty()) && (t.charAt(0) == '{' || t.charAt(0) == '[');
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    private static String getTrimmed(JsonNode obj, String field) {
        if (obj == null) {
            return null;
        }
        JsonNode n = obj.get(field);
        if (n == null || n.isNull()) {
            return null;
        }
        String v = n.asText();
        return v == null ? null : v.trim();
    }

    public JsonNode search(String query, int count, int offset) {
        McpSyncClient braveClient = resolveBraveClient();

        CallToolRequest request = new CallToolRequest(
            BRAVE_WEB_TOOL,
            Map.of("query", query, "count", count, "offset", offset)
        );

        CallToolResult result = braveClient.callTool(request);

        JsonNode raw = objectMapper.valueToTree(result.content());
        log.info("[Brave MCP Response Raw content]: {}", raw);

        ArrayNode normalized = objectMapper.createArrayNode();
        normalizeContent(raw, normalized);

        ObjectNode root = objectMapper.createObjectNode();
        root.set("results", normalized);

        log.info("Brave 검색 결과 정규화 완료: {}건", normalized.size());
        return root;
    }

    /**
     * MCP 클라이언트들 중 brave_web_search 툴을 가진 클라이언트 선택. 초기화되지 않은 경우 1회 initialize() 후 재시도.
     */
    private McpSyncClient resolveBraveClient() {
        for (McpSyncClient client : mcpClients) {
            try {
                ListToolsResult tools = client.listTools();
                if (hasBraveTool(tools)) {
                    return client;
                }
            } catch (McpError e) {
                String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase(Locale.ROOT);
                boolean likelyUninitialized = msg.contains("not initialized")
                    || msg.contains("uninitialized")
                    || (msg.contains("initialize") && msg.contains("required"));
                if (likelyUninitialized) {
                    // 1회만 초기화 재시도
                    try {
                        log.warn("MCP 클라이언트 미초기화 감지 → initialize() 재시도");
                        client.initialize();
                        ListToolsResult tools = client.listTools();
                        if (hasBraveTool(tools)) {
                            return client;
                        }
                    } catch (Exception initError) {
                        log.error("MCP 클라이언트 초기화 실패: {}", initError.getMessage());
                    }
                } else {
                    log.debug("listTools() 예외: {}", e.getMessage());
                }
            } catch (Exception e) {
                log.debug("listTools() 일반 예외: {}", e.getMessage());
            }
        }
        throw new IllegalStateException("Brave MCP 서버에서 brave_web_search 툴을 찾을 수 없습니다.");
    }

    private boolean hasBraveTool(ListToolsResult tools) {
        return tools != null && tools.tools() != null &&
            tools.tools().stream().anyMatch(t -> BRAVE_WEB_TOOL.equalsIgnoreCase(t.name()));
    }

    /**
     * raw JSON(any shape) → results[{url,title,description}]
     */
    private void normalizeContent(JsonNode raw, ArrayNode out) {
        if (raw == null || raw.isNull()) {
            return;
        }

        if (raw.isArray()) {
            for (JsonNode n : raw) {
                normalizeOne(n, out);
            }
        } else {
            normalizeOne(raw, out);
        }
    }

    private void normalizeOne(JsonNode node, ArrayNode out) {
        if (node == null || node.isNull()) {
            return;
        }

        // 이미 {url,title,description} 형태
        if (node.isObject() && (node.has("url") || node.has("title") || node.has("description"))) {
            addObjectToResults((ObjectNode) node, out);
            return;
        }

        // MCP가 주는 content item: { "type":"text", "text":"{...json...}" } 같은 형태를 방어
        if (node.isObject() && node.has("text")) {
            String text = node.path("text").asText("");
            if (looksLikeJson(text)) {
                parseJsonBlockIntoResults(text, out, 0);
            } else {
                // "Title: ..." 라인 포맷 등 레거시 텍스트 포맷 처리(옵션)
                parseLegacyBlock(text, out);
            }
            return;
        }

        // 순수 문자열이지만 안에 JSON이 들어 있는 경우
        if (node.isTextual() && looksLikeJson(node.asText())) {
            parseJsonBlockIntoResults(node.asText(), out, 0);
        }
    }

    private void parseJsonBlockIntoResults(String json, ArrayNode out, int depth) {
        if (depth > MAX_JSON_RECURSION_DEPTH) {
            log.warn("JSON 파싱 재귀 깊이 초과: {}", truncate(json, 100));
            return;
        }
        try {
            JsonNode parsed = objectMapper.readTree(json);

            if (parsed.isArray()) {
                for (JsonNode n : parsed) {
                    if (n.isObject()) {
                        addObjectToResults((ObjectNode) n, out);
                    } else if (n.isTextual() && looksLikeJson(n.asText())) {
                        parseJsonBlockIntoResults(n.asText(), out, depth + 1);
                    }
                }
            } else if (parsed.isObject()) {
                ObjectNode obj = (ObjectNode) parsed;
                // 루트에 results 배열이 있는 형태 처리
                if (obj.has("results") && obj.get("results").isArray()) {
                    for (JsonNode n : obj.get("results")) {
                        if (n.isObject()) {
                            addObjectToResults((ObjectNode) n, out);
                        } else if (n.isTextual() && looksLikeJson(n.asText())) {
                            parseJsonBlockIntoResults(n.asText(), out, depth + 1);
                        }
                    }
                    return;
                }
                if (obj.has("text") && obj.get("text").isTextual() && looksLikeJson(
                    obj.get("text").asText())) {
                    // {text:"{...}"} 같은 한 번 더 감싼 케이스
                    parseJsonBlockIntoResults(obj.get("text").asText(), out, depth + 1);
                } else {
                    addObjectToResults(obj, out);
                }
            } else if (parsed.isTextual() && looksLikeJson(parsed.asText())) {
                parseJsonBlockIntoResults(parsed.asText(), out, depth + 1);
            }
        } catch (Exception e) {
            log.warn("Brave MCP JSON 파싱 실패: {}\n원인: {}", truncate(json, 400), e.getMessage());
        }
    }

    private void addObjectToResults(ObjectNode obj, ArrayNode out) {
        String url = getTrimmed(obj, "url");
        String title = getTrimmed(obj, "title");
        String desc = getTrimmed(obj, "description");

        // 세 필드 중 하나라도 있으면 결과로 채택
        if ((url != null && !url.isBlank()) ||
            (title != null && !title.isBlank()) ||
            (desc != null && !desc.isBlank())) {

            ObjectNode one = objectMapper.createObjectNode();
            if (url != null) {
                one.put("url", url);
            }
            if (title != null) {
                one.put("title", title);
            }
            if (desc != null) {
                one.put("description", desc);
            }
            out.add(one);
        }
    }

    // 레거시 "Title:..., URL:..., 본문..." 형태의 텍스트 파서(있으면 도움, 없어도 무방)
    private void parseLegacyBlock(String text, ArrayNode out) {
        if (text == null || text.isBlank()) {
            return;
        }

        String[] lines = text.split("\\r?\\n");
        String title = null, url = null;
        StringBuilder body = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.regionMatches(true, 0, "Title:", 0, 6)) {
                if (title != null && url != null && body.length() > 0) {
                    ObjectNode one = objectMapper.createObjectNode();
                    one.put("title", title);
                    one.put("url", url);
                    one.put("description", body.toString().trim());
                    out.add(one);
                    body.setLength(0);
                }
                title = trimmed.substring(6).trim();
            } else if (trimmed.regionMatches(true, 0, "URL:", 0, 4)) {
                url = trimmed.substring(4).trim();
            } else {
                body.append(line).append('\n');
            }
        }

        if (title != null && url != null) {
            ObjectNode one = objectMapper.createObjectNode();
            one.put("title", title);
            one.put("url", url);
            one.put("description", body.toString().trim());
            out.add(one);
        }
    }
}
