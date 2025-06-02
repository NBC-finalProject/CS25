package com.example.cs25.global.jwt.provider;

import com.example.cs25.global.dto.Role;
import com.example.cs25.global.jwt.dto.TokenResponseDto;
import com.example.cs25.global.jwt.exception.JwtAuthenticationException;
import com.example.cs25.global.jwt.exception.JwtExceptionCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
@NoArgsConstructor
public class JwtTokenProvider {

    private final MacAlgorithm algorithm = Jwts.SIG.HS256;
    @Value("${jwt.secret-key}")
    private String secret;
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;
    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String email, String nickname, Role role) {
        return createToken(userId.toString(), email, nickname, role, accessTokenExpiration);
    }

    public String generateRefreshToken(Long userId, String email, String nickname, Role role) {
        return createToken(userId.toString(), email, nickname, role, refreshTokenExpiration);
    }

    public TokenResponseDto generateTokenPair(Long userId, String email, String nickname, Role role) {
        String accessToken = generateAccessToken(userId, email, nickname, role);
        String refreshToken = generateRefreshToken(userId, email, nickname, role);
        return new TokenResponseDto(accessToken, refreshToken);
    }

    private String createToken(String subject, String email, String nickname, Role role, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        var builder = Jwts.builder()
            .subject(subject)
            .issuedAt(now)
            .expiration(expiry);

        if (email != null) builder.claim("email", email);
        if (nickname != null) builder.claim("nickname", nickname);
        if (role != null) builder.claim("role", role.name());

        return builder
            .signWith(key, algorithm)
            .compact();
    }

    public boolean validateToken(String token) throws JwtAuthenticationException {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;

        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException(JwtExceptionCode.EXPIRED_TOKEN);

        } catch (SecurityException | MalformedJwtException e) {
            throw new JwtAuthenticationException(JwtExceptionCode.INVALID_SIGNATURE);

        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtAuthenticationException(JwtExceptionCode.INVALID_TOKEN);
        }
    }

    private Claims parseClaims(String token) throws JwtAuthenticationException {
        try {
            return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 재발급용
        } catch (Exception e) {
            throw new JwtAuthenticationException(JwtExceptionCode.INVALID_TOKEN);
        }
    }

    public Long getAuthorId(String token) throws JwtAuthenticationException {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public String getEmail(String token) throws JwtAuthenticationException {
        return parseClaims(token).get("email", String.class);
    }

    public String getNickname(String token) throws JwtAuthenticationException {
        return parseClaims(token).get("nickname", String.class);
    }

    public Role getRole(String token) throws JwtAuthenticationException {
        String roleStr = parseClaims(token).get("role", String.class);
        if (roleStr == null) {
            throw new JwtAuthenticationException(JwtExceptionCode.INVALID_TOKEN);
        }
        return Role.valueOf(roleStr);
    }

    public long getRemainingExpiration(String token) throws JwtAuthenticationException {
        return parseClaims(token).getExpiration().getTime() - System.currentTimeMillis();
    }

    public Duration getRefreshTokenDuration() {
        return Duration.ofMillis(refreshTokenExpiration);
    }

}