package com.example.cs25.global.jwt.filter;

import com.example.cs25.domain.users.entity.AuthUser;
import com.example.cs25.domain.users.entity.Role;
import com.example.cs25.global.jwt.exception.JwtAuthenticationException;
import com.example.cs25.global.jwt.provider.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.List;

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

                    AuthUser authUser = new AuthUser(userId, email,nickname , role);
                    GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());

                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(authUser, null, List.of(authority));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtAuthenticationException e) {
                throw new RuntimeException(e);
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