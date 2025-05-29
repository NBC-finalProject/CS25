package com.example.cs25.global.handler;

import com.example.cs25.domain.users.entity.AuthUser;
import com.example.cs25.global.jwt.dto.TokenResponseDto;
import com.example.cs25.global.jwt.provider.JwtTokenProvider;
import com.example.cs25.global.jwt.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication) throws IOException{

        try {
            AuthUser authUser = (AuthUser) authentication.getPrincipal();
            log.info("OAuth 로그인 성공: {}", authUser.getEmail());

            String accessToken = jwtTokenProvider.generateAccessToken(authUser.getId(), authUser.getEmail(),authUser.getName() , authUser.getRole());
            String refreshToken = jwtTokenProvider.generateRefreshToken(authUser.getId(), authUser.getEmail(),authUser.getName() , authUser.getRole());

            refreshTokenService.save(authUser.getId(), refreshToken, jwtTokenProvider.getRefreshTokenDuration());

            TokenResponseDto tokenResponse = new TokenResponseDto(accessToken, refreshToken);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(objectMapper.writeValueAsString(tokenResponse));

        } catch (Exception e) {
            log.error(" OAuth2 로그인 처리 중 에러 발생", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인 실패");
        }
    }

}
