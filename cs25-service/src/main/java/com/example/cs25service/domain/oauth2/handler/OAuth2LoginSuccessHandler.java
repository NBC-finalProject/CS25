package com.example.cs25service.domain.oauth2.handler;

import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.security.jwt.dto.TokenResponseDto;
import com.example.cs25service.domain.security.jwt.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final TokenService tokenService;

    private boolean cookieSecure = true; // 배포시에는 true로 변경해야함

    @Value("${FRONT_END_URI:http://localhost:5173}")
    private String frontEndUri;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication) throws IOException {

        try {
            AuthUser authUser = (AuthUser) authentication.getPrincipal();
            log.info("OAuth 로그인 성공: {}", authUser.getEmail());

            TokenResponseDto tokenResponse = tokenService.generateAndSaveTokenPair(authUser);

            // 쿠키 생성 - 보안 설정에 따라 Secure, SameSite 옵션 등 조정 가능
            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken",
                    tokenResponse.getAccessToken())
                .httpOnly(true) // XSS 방지
                .secure(cookieSecure) // HTTPS 통신만 가능
                .path("/") // 전체 경로에서 쿠키 유효
                .maxAge(Duration.ofMillis(accessTokenExpiration)) // 원하는 만료 시간
                .sameSite("Lax") // GET 호출에만 쿠키 전송
                .build();

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken",
                    tokenResponse.getRefreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofMillis(refreshTokenExpiration)) // 원하는 만료 시간
                .sameSite("Lax")
                .build();

            log.info("OAuth2 로그인 응답헤더에 쿠키 추가 완료");

            // 응답 헤더에 쿠키 추가
            response.setHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            response.sendRedirect(frontEndUri);

        } catch (Exception e) {
            log.error("OAuth2 로그인 처리 중 에러 발생", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인 실패");
        }
    }
}
