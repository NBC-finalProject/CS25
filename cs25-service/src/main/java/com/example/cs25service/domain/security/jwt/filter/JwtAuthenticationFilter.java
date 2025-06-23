package com.example.cs25service.domain.security.jwt.filter;

import com.example.cs25common.global.exception.ErrorResponseUtil;
import com.example.cs25entity.domain.user.entity.Role;
import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.security.jwt.exception.JwtAuthenticationException;
import com.example.cs25service.domain.security.jwt.provider.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
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
        //System.out.println("[JwtFilter] URI: " + request.getRequestURI() + ", Token: " + token);

        if (token != null) {
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    String userId = jwtTokenProvider.getAuthorId(token);
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
                ErrorResponseUtil.writeJsonError(response, e.getHttpStatus().value(),
                    e.getMessage());
                // SecurityContext를 설정하지 않고 다음 필터로 진행
                // 인증이 필요한 엔드포인트에서는 별도 처리됨
                return;
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
}