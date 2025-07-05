package com.example.cs25service.domain.security.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.Arrays;
import org.apache.commons.text.StringEscapeUtils;

public class XssRequestWrapper extends HttpServletRequestWrapper {

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
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
}
