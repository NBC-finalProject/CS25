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

    //@Value("${FRONT_END_URI:http://localhost:5173}")
    private String frontEndUri = "http://localhost:8080";

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

//            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
//            response.setStatus(HttpServletResponse.SC_OK);

            //response.getWriter().write(objectMapper.writeValueAsString(tokenResponse));

            //프론트 생기면 추가 -> 헤더에 바로 jwt 꼽아넣어서 하나하나 jwt 적용할 필요가 없어짐
            // 쿠키 생성 - 보안 설정에 따라 Secure, SameSite 옵션 등 조정 가능
            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken",
                    tokenResponse.getAccessToken())
                .httpOnly(true)
                .secure(true) // HTTPS가 아닐 경우 false
                .path("/")
                .maxAge(Duration.ofMillis(accessTokenExpiration)) // 원하는 만료 시간
                .sameSite("None") // 필요에 따라 "Lax", "None"
                .build();

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken",
                    tokenResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMillis(refreshTokenExpiration)) // 원하는 만료 시간
                .sameSite("None")
                .build();

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
