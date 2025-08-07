package com.example.cs25service.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
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

        return content != null ? content : objectMapper.createObjectNode();
    }

    private McpSyncClient resolveBraveClient() {
        for (McpSyncClient client : mcpClients) {
            ListToolsResult tools = client.listTools();
            if (tools != null && tools.tools() != null) {
                boolean found = tools.tools().stream()
                    .anyMatch(tool -> BRAVE_WEB_TOOL.equalsIgnoreCase(tool.name()));
                if (found) {
                    return client;
                }
            }
        }

        throw new IllegalStateException("Brave MCP 서버에서 brave_web_search 툴을 찾을 수 없습니다.");
    }
}
