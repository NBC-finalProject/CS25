package com.example.cs25service.domain.security.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.commons.text.StringEscapeUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XssRequestWrapper extends HttpServletRequestWrapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final String sanitizedJsonBody;
    private static final int MAX_DEPTH = 30;

    public XssRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);

        if (request.getContentType() != null && request.getContentType()
            .contains("application/json")) {
            String rawBody = request.getReader().lines()
                .collect(Collectors.joining(System.lineSeparator()));
            this.sanitizedJsonBody = sanitizeJsonBody(rawBody); //JSON 필드 값만 escape
        } else {
            this.sanitizedJsonBody = null;
        }
    }

    @Override
    public String getParameter(String name) {
        return escape(super.getParameter(name));
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        return Arrays.stream(values).map(this::escape).toArray(String[]::new);
    }

    private String escape(String input) {
        return input == null ? null : StringEscapeUtils.escapeHtml4(input);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (sanitizedJsonBody == null) {
            return super.getInputStream();
        }

        byte[] bytes = sanitizedJsonBody.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
            }

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (sanitizedJsonBody == null) {
            return super.getReader();
        }
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    // JSON 필드 값만 escape하는 메서드
    private String sanitizeJsonBody(String rawBody) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(rawBody);
            sanitizeJsonNode(root);
            return OBJECT_MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            // 문제가 생기면 원본 반환 (fallback)
            log.error("Failed to sanitize JSON body", e);
            return rawBody;
        }
    }

    private void sanitizeJsonNode(JsonNode node) {
        sanitizeJsonNode(node, 0);
    }

    private void sanitizeJsonNode(JsonNode node, int depth) {
        if (depth > MAX_DEPTH) {
            throw new IllegalArgumentException("JSON 깊이가 30이상입니다. DoS 공격이 의심됩니다.");
        }
        if (node.isObject()) {
            ObjectNode objNode = (ObjectNode) node;
            objNode.fieldNames().forEachRemaining(field -> {
                JsonNode child = objNode.get(field);
                if (child.isTextual()) {
                    String sanitized = StringEscapeUtils.escapeHtml4(child.asText());
                    objNode.put(field, sanitized);
                } else {
                    sanitizeJsonNode(child, depth + 1);
                }
            });
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                sanitizeJsonNode(item, depth + 1);
            }
        }
    }
}
