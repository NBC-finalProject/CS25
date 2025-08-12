package com.example.cs25service.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BraveSearchMcpService {

    private static final String BRAVE_WEB_TOOL = "brave_web_search";
    private static final Duration INIT_TIMEOUT = Duration.ofSeconds(60);
    private final List<McpSyncClient> mcpClients;
    private final ObjectMapper objectMapper;

    public JsonNode search(String query, int count, int offset) {
        McpSyncClient braveClient = resolveBraveClient();

        CallToolRequest request = new CallToolRequest(
            BRAVE_WEB_TOOL,
            Map.of("query", query, "count", count, "offset", offset)
        );

        CallToolResult result = braveClient.callTool(request);

        JsonNode content = objectMapper.valueToTree(result.content());
        log.info("[Brave MCP Response Raw content]: {}", content.toPrettyString());

        if (content != null && content.isArray()) {
            var root = objectMapper.createObjectNode();
            root.set("results", content);
            return root;

        }
    }

    private void ensureInitialized(McpSyncClient client) {
        if (!client.isInitialized()) {
            synchronized (client) {               // 다중 스레드 초기화 경합 방지
                if (!client.isInitialized()) {
                    log.debug("MCP 클라이언트 초기화 시작…");
                    client.initialize();          // 매개변수 없는 버전
                    log.debug("MCP 클라이언트 초기화 완료");
                }
            }
        }
    }

    private McpSyncClient resolveBraveClient() {
        for (McpSyncClient client : mcpClients) {
            try {
                ensureInitialized(client);                // 초기화
                ListToolsResult tools = client.listTools();
                if (tools != null && tools.tools() != null &&
                    tools.tools().stream()
                        .anyMatch(t -> BRAVE_WEB_TOOL.equalsIgnoreCase(t.name()))) {
                    return client;
                }
            } catch (Exception e) {
                log.debug("Brave MCP 클라이언트 후보 실패: {}", e.toString());
            }
        }
        throw new IllegalStateException("Brave MCP 서버에서 '" + BRAVE_WEB_TOOL + "' 툴을 찾을 수 없습니다.");
    }
}