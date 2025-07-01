package com.example.cs25service.domain.security.jwt.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService")
class RefreshTokenServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private final String userId = "user123";
    private final String token = "refresh-token";
    private final Duration ttl = Duration.ofMinutes(10);
    private final String key = "RT:" + userId;

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("refresh token을 저장한다")
        void saveToken_success() {
            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            refreshTokenService.save(userId, token, ttl);

            verify(valueOperations).set(key, token, ttl);
        }

        @Test
        @DisplayName("TTL이 null이면 예외를 던진다")
        void saveToken_nullTtl() {
            assertThatThrownBy(() -> refreshTokenService.save(userId, token, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("TTL must not be null");
        }
    }

    @Nested
    @DisplayName("get()")
    class GetTest {

        @Test
        @DisplayName("저장된 refresh token을 조회한다")
        void getToken_success() {
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(key)).willReturn(token);

            String result = refreshTokenService.get(userId);

            assertThat(result).isEqualTo(token);
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("refresh token을 삭제한다")
        void deleteToken_success() {
            refreshTokenService.delete(userId);

            verify(redisTemplate).delete(key);
        }
    }

    @Nested
    @DisplayName("exists()")
    class ExistsTest {

        @Test
        @DisplayName("해당 유저의 refresh token이 존재하면 true 반환")
        void tokenExists() {
            given(redisTemplate.hasKey(key)).willReturn(true);

            boolean result = refreshTokenService.exists(userId);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("refresh token이 없으면 false 반환")
        void tokenNotExists() {
            given(redisTemplate.hasKey(key)).willReturn(false);

            boolean result = refreshTokenService.exists(userId);

            assertThat(result).isFalse();
        }
    }
}
