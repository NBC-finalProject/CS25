package com.example.cs25service.domain.users.service;


import com.example.cs25entity.domain.user.entity.Role;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.exception.UserExceptionCode;
import com.example.cs25service.domain.security.jwt.dto.ReissueRequestDto;
import com.example.cs25service.domain.security.jwt.dto.TokenResponseDto;
import com.example.cs25service.domain.security.jwt.exception.JwtAuthenticationException;
import com.example.cs25service.domain.security.jwt.provider.JwtTokenProvider;
import com.example.cs25service.domain.security.jwt.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public TokenResponseDto reissue(ReissueRequestDto reissueRequestDto)
        throws JwtAuthenticationException {
        String refreshToken = reissueRequestDto.getRefreshToken();

        String userId = jwtTokenProvider.getAuthorId(refreshToken);
        //String email = jwtTokenProvider.getEmail(refreshToken);
        String nickname = jwtTokenProvider.getNickname(refreshToken);
        Role role = jwtTokenProvider.getRole(refreshToken);

        // 2. Redis 에 저장된 토큰 조회
        String savedToken = refreshTokenService.get(userId);
        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw new UserException(UserExceptionCode.TOKEN_NOT_MATCHED);
        }

        // 4. 새 토큰 발급
        TokenResponseDto newToken = jwtTokenProvider.generateTokenPair(userId, nickname, role);

        // 5. Redis 갱신
        refreshTokenService.save(userId, newToken.getRefreshToken(),
            jwtTokenProvider.getRefreshTokenDuration());

        return newToken;
    }

    public void logout(String userId) {
        if (!refreshTokenService.exists(userId)) {
            throw new UserException(UserExceptionCode.TOKEN_NOT_MATCHED);
        }
        refreshTokenService.delete(userId);
        SecurityContextHolder.clearContext();
    }

}
