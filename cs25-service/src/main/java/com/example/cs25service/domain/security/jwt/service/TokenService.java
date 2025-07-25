package com.example.cs25service.domain.security.jwt.service;

import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.security.jwt.dto.TokenResponseDto;
import com.example.cs25service.domain.security.jwt.provider.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public TokenResponseDto generateAndSaveTokenPair(AuthUser authUser) {
        String accessToken = jwtTokenProvider.generateAccessToken(
            authUser.getSerialId(), authUser.getName(), authUser.getRole()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(
            authUser.getSerialId(), authUser.getName(), authUser.getRole()
        );
        refreshTokenService.save(authUser.getSerialId(), refreshToken,
            jwtTokenProvider.getRefreshTokenDuration());

        return new TokenResponseDto(accessToken, refreshToken);
    }


    public ResponseCookie createAccessTokenCookie(String accessToken) {
        return ResponseCookie.from("accessToken", accessToken)
            .httpOnly(false) //프론트 생기면 true
            .secure(false) //https 적용되면 true
            .path("/")
            .maxAge(Duration.ofMinutes(60))
            .sameSite("Lax")
            .build();
    }

    public void clearTokenForUser(String userId, HttpServletResponse response) {
        // 1. Redis refreshToken 삭제
        refreshTokenService.delete(userId);

        // 2. accessToken 쿠키 만료 설정
        ResponseCookie expiredCookie = ResponseCookie.from("accessToken", "")
            .httpOnly(false)
            .secure(false)
            .path("/")
            .maxAge(0)
            .sameSite("Lax")
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
    }
}
