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

public class XssRequestWrapper extends HttpServletRequestWrapper {

    private final String sanitizedJsonBody;

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

    // 🔽 JSON 필드 값만 escape하는 메서드
    private String sanitizeJsonBody(String rawBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawBody);
            sanitizeJsonNode(root);
            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            // 문제가 생기면 원본 반환 (fallback)
            return rawBody;
        }
    }

    private void sanitizeJsonNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objNode = (ObjectNode) node;
            objNode.fieldNames().forEachRemaining(field -> {
                JsonNode child = objNode.get(field);
                if (child.isTextual()) {
                    String sanitized = StringEscapeUtils.escapeHtml4(child.asText());
                    objNode.put(field, sanitized);
                } else {
                    sanitizeJsonNode(child);
                }
            });
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                sanitizeJsonNode(item);
            }
        }
    }
}
