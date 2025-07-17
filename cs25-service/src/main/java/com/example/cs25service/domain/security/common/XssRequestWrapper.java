package com.example.cs25service.domain.security.common;

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

    private final String sanitizedJsonBody; //결과 저장예정

    public XssRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);

        if (request.getContentType() != null && request.getContentType()
            .contains("application/json")) { //Request Body 가 다 제이슨이니깐
            String rawBody = request.getReader().lines()
                .collect(Collectors.joining(System.lineSeparator()));
            this.sanitizedJsonBody = StringEscapeUtils.escapeHtml4(rawBody); // 또는 필드 단위 escape
        } else {
            this.sanitizedJsonBody = null;
        }
    }

    @Override
    public String getParameter(String name) { //폼 요청(application/x-www-form-urlencoded)일 경우
        return escape(super.getParameter(name));
    }

    @Override
    public String[] getParameterValues(String name) { //getParameter << 이거 있을때
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
        //request Body가 있으면 스트림으로 다 일겅바야
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
}
