package com.example.cs25.global.jwt.filter;

import com.example.cs25.domain.users.entity.Role;
import com.example.cs25.global.dto.AuthUser;
import com.example.cs25.global.jwt.exception.JwtAuthenticationException;
import com.example.cs25.global.jwt.provider.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null) {
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    Long userId = jwtTokenProvider.getAuthorId(token);
                    String email = jwtTokenProvider.getEmail(token);
                    String nickname = jwtTokenProvider.getNickname(token);
                    Role role = jwtTokenProvider.getRole(token);

                    AuthUser authUser = new AuthUser(userId, email, nickname, role);

                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(authUser, null,
                            authUser.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtAuthenticationException e) {
                // 로그 기록 후 인증 실패 처리
                logger.info("인증 실패", e);
                handleJwtError(response, e.getHttpStatus().value(), e.getMessage());
                // SecurityContext를 설정하지 않고 다음 필터로 진행
                // 인증이 필요한 엔드포인트에서는 별도 처리됨
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1. Authorization 헤더 우선
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 2. 쿠키에서도 accessToken 찾아보기
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private void handleJwtError(HttpServletResponse response, int statusCode, String message)
        throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("code", statusCode);
        errorBody.put("status", HttpStatus.valueOf(statusCode).name());
        errorBody.put("message", message);

        String json = new ObjectMapper().writeValueAsString(errorBody);
        response.getWriter().write(json);
    }
}