package com.example.cs25service.domain.security.jwt.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.cs25entity.domain.user.entity.Role;
import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.security.jwt.dto.TokenResponseDto;
import com.example.cs25service.domain.security.jwt.provider.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

@DisplayName("TokenService")
class TokenServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("generateAndSaveTokenPair()")
    class GenerateAndSaveTokenPairTest {

        @Test
        @DisplayName("AccessToken과 RefreshToken을 생성하고 저장한 후 반환한다")
        void generateAndSaveTokenPair_success() {
            // given
            AuthUser user = new AuthUser("nickname", "user123", Role.USER);
            String accessToken = "access-token";
            String refreshToken = "refresh-token";

            given(
                jwtTokenProvider.generateAccessToken("user123", "nickname", Role.USER)).willReturn(
                accessToken);
            given(
                jwtTokenProvider.generateRefreshToken("user123", "nickname", Role.USER)).willReturn(
                refreshToken);
            given(jwtTokenProvider.getRefreshTokenDuration()).willReturn(Duration.ofDays(7));

            // when
            TokenResponseDto result = tokenService.generateAndSaveTokenPair(user);

            // then
            assertThat(result.getAccessToken()).isEqualTo(accessToken);
            assertThat(result.getRefreshToken()).isEqualTo(refreshToken);
            verify(refreshTokenService).save("user123", refreshToken, Duration.ofDays(7));
        }
    }

    @Nested
    @DisplayName("createAccessTokenCookie()")
    class CreateAccessTokenCookieTest {

        @Test
        @DisplayName("accessToken 쿠키를 생성하여 반환한다")
        void createCookie_success() {
            // given
            String token = "access-token";

            // when
            ResponseCookie cookie = tokenService.createAccessTokenCookie(token);

            // then
            assertThat(cookie.getName()).isEqualTo("accessToken");
            assertThat(cookie.getValue()).isEqualTo("access-token");
            assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(60 * 60);
            assertThat(cookie.isHttpOnly()).isTrue(); // 프론트 연동 시 true
            assertThat(cookie.getPath()).isEqualTo("/");
        }
    }

    @Nested
    @DisplayName("clearTokenForUser()")
    class ClearTokenForUserTest {

        @Test
        @DisplayName("Redis에서 리프레시 토큰을 삭제하고 만료된 accessToken 쿠키를 응답에 추가한다")
        void clearToken_success() {
            // given
            String userId = "user123";
            HttpServletResponse response = mock(HttpServletResponse.class);

            ArgumentCaptor<String> headerNameCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);

            // when
            tokenService.clearTokenForUser(userId, response);

            // then
            verify(refreshTokenService).delete(userId);
            verify(response).addHeader(headerNameCaptor.capture(), headerValueCaptor.capture());

            assertThat(headerNameCaptor.getValue()).isEqualTo(HttpHeaders.SET_COOKIE);
            assertThat(headerValueCaptor.getValue()).contains("accessToken=");
            assertThat(headerValueCaptor.getValue()).contains("Max-Age=0");
        }
    }
}
