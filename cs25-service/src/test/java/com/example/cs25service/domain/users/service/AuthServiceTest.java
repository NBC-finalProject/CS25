package com.example.cs25service.domain.users.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.cs25entity.domain.user.entity.Role;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25service.domain.security.jwt.dto.ReissueRequestDto;
import com.example.cs25service.domain.security.jwt.dto.TokenResponseDto;
import com.example.cs25service.domain.security.jwt.provider.JwtTokenProvider;
import com.example.cs25service.domain.security.jwt.service.RefreshTokenService;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    private final String userId = "user123";
    private final String nickname = "tester";
    private final String refreshToken = "refresh.token.value";
    private final String newRefreshToken = "new.refresh.token";
    private final Role role = Role.USER;

    @Nested
    @DisplayName("reissue")
    class Reissue {

        @Test
        @DisplayName("리프레시 토큰이 유효하면 새로운 토큰을 반환한다")
        void reissue_success() {
            // given
            ReissueRequestDto dto = new ReissueRequestDto(refreshToken);
            TokenResponseDto newToken = new TokenResponseDto("new.access.token", newRefreshToken);

            given(jwtTokenProvider.getAuthorId(refreshToken)).willReturn(userId);
            given(jwtTokenProvider.getNickname(refreshToken)).willReturn(nickname);
            given(jwtTokenProvider.getRole(refreshToken)).willReturn(role);
            given(refreshTokenService.get(userId)).willReturn(refreshToken);
            given(jwtTokenProvider.generateTokenPair(userId, nickname, role)).willReturn(newToken);
            given(jwtTokenProvider.getRefreshTokenDuration()).willReturn(Duration.ofDays(7));

            // when
            TokenResponseDto result = authService.reissue(dto);

            // then
            assertThat(result.getAccessToken()).isEqualTo("new.access.token");
            assertThat(result.getRefreshToken()).isEqualTo(newRefreshToken);
            verify(refreshTokenService).save(eq(userId), eq(newRefreshToken), any(Duration.class));
        }

        @Test
        @DisplayName("저장된 토큰과 다르면 예외가 발생한다")
        void reissue_tokenMismatch() {
            // given
            ReissueRequestDto dto = new ReissueRequestDto(refreshToken);
            given(jwtTokenProvider.getAuthorId(refreshToken)).willReturn(userId);
            given(jwtTokenProvider.getNickname(refreshToken)).willReturn(nickname);
            given(jwtTokenProvider.getRole(refreshToken)).willReturn(role);
            given(refreshTokenService.get(userId)).willReturn("invalid.token");

            // when & then
            assertThatThrownBy(() -> authService.reissue(dto))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("유효한 리프레시 토큰 값이 아닙니다.");
        }

        @Test
        @DisplayName("Redis에 저장된 토큰이 null이면 예외가 발생한다")
        void reissue_tokenNull() {
            // given
            ReissueRequestDto dto = new ReissueRequestDto(refreshToken);
            given(jwtTokenProvider.getAuthorId(refreshToken)).willReturn(userId);
            given(jwtTokenProvider.getNickname(refreshToken)).willReturn(nickname);
            given(jwtTokenProvider.getRole(refreshToken)).willReturn(role);
            given(refreshTokenService.get(userId)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> authService.reissue(dto))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("유효한 리프레시 토큰 값이 아닙니다.");
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("로그아웃 성공 시 Redis 토큰 삭제 및 컨텍스트 클리어")
        void logout_success() {
            // given
            given(refreshTokenService.exists(userId)).willReturn(true);

            // when
            authService.logout(userId);

            // then
            verify(refreshTokenService).delete(userId);
        }

        @Test
        @DisplayName("Redis에 토큰 없으면 예외 발생")
        void logout_tokenNotExist() {
            // given
            given(refreshTokenService.exists(userId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.logout(userId))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("유효한 리프레시 토큰 값이 아닙니다.");
        }
    }
}
