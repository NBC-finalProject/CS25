package com.example.cs25service.domain.security.jwt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SameSiteCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {
        HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(response) {
            @Override
            public void addHeader(String name, String value) {
                if ("Set-Cookie".equalsIgnoreCase(name) && value.startsWith("JSESSIONID=")) {
                    // SameSite와 Secure 속성이 없는 경우에만 추가
                    if (!value.contains("SameSite=")) {
                        value = value + "; SameSite=None";
                    }
                    if (!value.contains("Secure")) {
                        value = value + "; Secure";
                    }
                }
                super.addHeader(name, value);
            }
        };
        filterChain.doFilter(request, wrapper);
    }
}