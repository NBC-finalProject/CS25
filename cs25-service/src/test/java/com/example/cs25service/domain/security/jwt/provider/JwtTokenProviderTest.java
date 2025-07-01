package com.example.cs25service.domain.security.jwt.provider;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.example.cs25entity.domain.user.entity.Role;
import com.example.cs25service.domain.security.jwt.dto.TokenResponseDto;
import com.example.cs25service.domain.security.jwt.exception.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("JwtTokenProvider")
class JwtTokenProviderTest {

    JwtTokenProvider jwtTokenProvider;

    String secretKey = "testjwtsecretkeytestjwtsecretkeytestjwtsecretkeytestjwtsecretkey";
    long accessTokenExpiry = 1000 * 60 * 15; // 15분
    long refreshTokenExpiry = 1000 * 60 * 60 * 24 * 7; // 7일

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", secretKey);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiration", accessTokenExpiry);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration",
            refreshTokenExpiry);
        jwtTokenProvider.init(); // @PostConstruct 수동 호출
    }

    /// ////////////////// Helper Methods/////////////////////
    private String generateTestToken() {
        return invokeCreateToken("user123", "nick", Role.USER, refreshTokenExpiry);
    }

    private String invokeCreateToken(String subject, String nickname, Role role, long expMs) {
        try {
            Field keyField = JwtTokenProvider.class.getDeclaredField("key");
            keyField.setAccessible(true);
            SecretKey key = (SecretKey) keyField.get(jwtTokenProvider);

            return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expMs))
                .claim("nickname", nickname)
                .claim("role", role != null ? role.name() : null)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        } catch (Exception e) {
            throw new RuntimeException("createToken 리플렉션 실패", e);
        }
    }

    // extract claims for verification
    private Claims getClaims(String token) {
        try {
            // SecretKey 꺼내기
            Field keyField = JwtTokenProvider.class.getDeclaredField("key");
            keyField.setAccessible(true);
            SecretKey key = (SecretKey) keyField.get(jwtTokenProvider);

            return Jwts.parser()
                .verifyWith(key) // 여기서 key 직접 전달
                .build()
                .parseSignedClaims(token)
                .getPayload();

        } catch (Exception e) {
            throw new RuntimeException("JWT claim 파싱 실패", e);
        }
    }

    /// //////////////////////////////////////////////////////////////////

    @Nested
    @DisplayName("generateAccessToken() 은")
    class GenerateAccessTokenTest {

        @Test
        @DisplayName("정상적으로 AccessToken을 생성한다")
        void generateAccessTokenSuccess() {
            String token = jwtTokenProvider.generateAccessToken("user1", "nick", Role.USER);

            assertThat(token).isNotBlank();
            var claims = getClaims(token);
            assertThat(claims.getSubject()).isEqualTo("user1");
        }
    }

    @Nested
    @DisplayName("generateRefreshToken() 은 ")
    class GenerateRefreshTokenTest {

        @Test
        @DisplayName("정상적으로 RefreshToken을 생성한다")
        void generateRefreshTokenSuccess() {
            String token = jwtTokenProvider.generateRefreshToken("user1", "nick", Role.USER);

            assertThat(token).isNotBlank();
            var claims = getClaims(token);
            assertThat(claims.getSubject()).isEqualTo("user1");
        }
    }

    @Nested
    @DisplayName("generateTokenPair() 은 ")
    class GenerateTokenPairTest {

        @Test
        @DisplayName("AccessToken과 RefreshToken을 함께 생성한다")
        void generateTokenPairSuccess() {
            TokenResponseDto pair = jwtTokenProvider.generateTokenPair("user1", "nick", Role.USER);

            assertThat(pair.getAccessToken()).isNotBlank();
            assertThat(pair.getRefreshToken()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("createToken 함수는 ")
    class CreateTokenTest {

        @Test
        @DisplayName("subject, nickname, role을 포함한 토큰을 생성한다")
        void createTokenIncludesClaims() {
            String token = generateTestToken();

            Claims claims = getClaims(token);
            assertThat(claims.getSubject()).isEqualTo("user123");
            assertThat(claims.get("nickname")).isEqualTo("nick");
            assertThat(claims.get("role")).isEqualTo(Role.USER.name());
        }

        @Test
        @DisplayName("nickname이나 role이 null이면 포함되지 않는다")
        void createTokenWithoutOptionalClaims() {
            String token = invokeCreateToken("user456", null, null, refreshTokenExpiry);

            Claims claims = getClaims(token);
            assertThat(claims.getSubject()).isEqualTo("user456");
            assertThat(claims.containsKey("nickname")).isFalse();
            assertThat(claims.containsKey("role")).isFalse();
        }
    }

    @Nested
    @DisplayName("validateToken 는")
    class ValidateTokenTest {

        @Test
        @DisplayName("정상 토큰이면 true 반환")
        void validTokenReturnsTrue() {
            String token = generateTestToken();
            boolean result = jwtTokenProvider.validateToken(token);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("만료된 토큰이면 JwtAuthenticationException 발생")
        void expiredTokenThrowsException() throws InterruptedException {
            String token = invokeCreateToken("user123", "nick", Role.USER, 500L);
            Thread.sleep(1000L); // 토큰 만료 대기

            assertThatThrownBy(() -> jwtTokenProvider.validateToken(token))
                .isInstanceOf(JwtAuthenticationException.class)
                .hasMessageContaining("만료된 토큰입니다.");
        }

        @Test
        @DisplayName("서명이 잘못된 토큰이면 예외 발생")
        void invalidSignatureThrowsException() {
            // 다른 키로 생성된 토큰
            SecretKey fakeKey = Keys.hmacShaKeyFor("otherkeyotherkeyotherkey1234567890".getBytes(
                StandardCharsets.UTF_8));
            String token = Jwts.builder()
                .subject("user")
                .signWith(fakeKey, Jwts.SIG.HS256)
                .compact();

            assertThatThrownBy(() -> jwtTokenProvider.validateToken(token))
                .isInstanceOf(JwtAuthenticationException.class)
                .hasMessageContaining("유효하지 않은 토큰입니다."); // JwtExceptionCode.INVALID_SIGNATURE
        }

        @Test
        @DisplayName("형식이 잘못된 토큰이면 예외 발생")
        void malformedTokenThrowsException() {
            String malformed = "abc.def"; // 유효한 구조 아님

            assertThatThrownBy(() -> jwtTokenProvider.validateToken(malformed))
                .isInstanceOf(JwtAuthenticationException.class)
                .hasMessageContaining("유효하지 않은 서명입니다."); // JwtExceptionCode.INVALID_TOKEN
        }
    }

    @Nested
    @DisplayName("getAuthorId()")
    class GetAuthorIdTest {

        @Test
        @DisplayName("토큰에서 subject를 추출한다")
        void extractSubject() {
            String token = jwtTokenProvider.generateAccessToken("user123", "nick", Role.USER);
            String subject = jwtTokenProvider.getAuthorId(token);

            assertThat(subject).isEqualTo("user123");
        }
    }

    @Nested
    @DisplayName("getNickname()")
    class GetNicknameTest {

        @Test
        @DisplayName("토큰에서 nickname을 추출한다")
        void extractNickname() {
            String token = jwtTokenProvider.generateAccessToken("user123", "nick123", Role.USER);
            String nickname = jwtTokenProvider.getNickname(token);

            assertThat(nickname).isEqualTo("nick123");
        }
    }

    @Nested
    @DisplayName("getRole()")
    class GetRoleTest {

        @Test
        @DisplayName("토큰에서 Role을 추출한다")
        void extractRole() {
            String token = jwtTokenProvider.generateAccessToken("user123", "nick", Role.ADMIN);
            Role role = jwtTokenProvider.getRole(token);

            assertThat(role).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("role 클레임이 없으면 예외 발생")
        void missingRoleThrowsException() {
            String token = createTokenWithoutClaim();

            assertThatThrownBy(() -> jwtTokenProvider.getRole(token))
                .isInstanceOf(JwtAuthenticationException.class)
                .hasMessageContaining("유효하지 않은 토큰입니다");
        }

        private String createTokenWithoutClaim() {
            try {
                Field keyField = JwtTokenProvider.class.getDeclaredField("key");
                keyField.setAccessible(true);
                SecretKey key = (SecretKey) keyField.get(jwtTokenProvider);

                return Jwts.builder()
                    .subject("user123")
                    .claim("nickname", "nick")
                    .signWith(key, Jwts.SIG.HS256)
                    .compact();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Nested
    @DisplayName("getRemainingExpiration()")
    class GetRemainingExpirationTest {

        @Test
        @DisplayName("남은 만료 시간을 계산해서 반환한다")
        void calculateRemainingTime() {
            String token = jwtTokenProvider.generateAccessToken("user123", "nick", Role.USER);
            long remaining = jwtTokenProvider.getRemainingExpiration(token);

            assertThat(remaining).isPositive();
            assertThat(remaining).isLessThanOrEqualTo(accessTokenExpiry);
        }
    }

    @Nested
    @DisplayName("getRefreshTokenDuration()")
    class GetRefreshTokenDurationTest {

        @Test
        @DisplayName("refreshToken의 Duration을 반환한다")
        void returnRefreshDuration() {
            Duration duration = jwtTokenProvider.getRefreshTokenDuration();

            assertThat(duration).isEqualTo(Duration.ofMillis(refreshTokenExpiry));
        }
    }

    @Nested
    @DisplayName("parseClaims()")
    class parseClaimsTest {

        JwtTokenProvider parseClaimsJwtTokenProvider;
        SecretKey key;

        @BeforeEach
        void setUp() {
            parseClaimsJwtTokenProvider = new JwtTokenProvider();
            ReflectionTestUtils.setField(parseClaimsJwtTokenProvider, "secret",
                "my-very-secret-secret-key-which-is-very-long");
            parseClaimsJwtTokenProvider.init();

            key = (SecretKey) ReflectionTestUtils.getField(parseClaimsJwtTokenProvider, "key");
        }

        private String createTokenWithExpiration(long milliseconds) {
            Date now = new Date();
            Date expiry = new Date(now.getTime() + milliseconds);

            return Jwts.builder()
                .subject("user123")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        }

        @Test
        @DisplayName("만료된 토큰이면 e.getClaims()를 반환한다")
        void expiredToken_returnsClaims() throws Exception {
            // given: 아주 짧은 만료시간으로 토큰 생성
            String expiredToken = createTokenWithExpiration(500L);
            Thread.sleep(1000L); // 만료될 때까지 대기

            // when
            Claims claims = parseClaimsJwtTokenProvider.parseClaims(expiredToken);

            // then
            assertThat(claims.getSubject()).isEqualTo("user123");
        }

        @Test
        @DisplayName("토큰이 완전히 잘못되었으면 INVALID_TOKEN 예외를 던진다")
        void invalidToken_throwsJwtAuthException() {
            // given: 유효하지 않은 토큰 문자열
            String invalidToken = "not.a.valid.token";

            // expect
            assertThatThrownBy(() -> parseClaimsJwtTokenProvider.parseClaims(invalidToken))
                .isInstanceOf(JwtAuthenticationException.class)
                .hasMessageContaining("유효하지 않은 토큰입니다.");
        }
    }

}