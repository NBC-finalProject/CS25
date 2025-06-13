package com.example.cs25.global.handler;

import com.example.cs25.global.dto.AuthUser;
import com.example.cs25.global.jwt.dto.TokenResponseDto;
import com.example.cs25.global.jwt.service.TokenService;
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

    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication) throws IOException {

        try {
            AuthUser authUser = (AuthUser) authentication.getPrincipal();
            log.info("OAuth 로그인 성공: {}", authUser.getEmail());

            TokenResponseDto tokenResponse = tokenService.generateAndSaveTokenPair(authUser);

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setStatus(HttpServletResponse.SC_OK);

            response.getWriter().write(objectMapper.writeValueAsString(tokenResponse));

            //프론트 생기면 추가 -> 헤더에 바로 jwt 꼽아넣어서 하나하나 jwt 적용할 필요가 없어짐
//            ResponseCookie accessTokenCookie =
//                tokenResponse.getAccessToken();
//
//            ResponseCookie refreshTokenCookie =
//                tokenResponse.getRefreshToken();
//
//            response.setHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
//            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        } catch (Exception e) {
            log.error("OAuth2 로그인 처리 중 에러 발생", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인 실패");
        }
    }
}
