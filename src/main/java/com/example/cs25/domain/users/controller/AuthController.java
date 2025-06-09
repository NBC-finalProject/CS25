package com.example.cs25.domain.users.controller;

import com.example.cs25.domain.users.service.AuthService;
import com.example.cs25.global.dto.ApiResponse;
import com.example.cs25.global.dto.AuthUser;
import com.example.cs25.global.jwt.dto.ReissueRequestDto;
import com.example.cs25.global.jwt.dto.TokenResponseDto;
import com.example.cs25.global.jwt.exception.JwtAuthenticationException;
import com.example.cs25.global.jwt.service.TokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    //프론트 생기면 할 것
//    @PostMapping("/reissue")
//    public ResponseEntity<ApiResponse<TokenResponseDto>> getSubscription(
//        @RequestBody ReissueRequestDto reissueRequestDto
//    ) throws JwtAuthenticationException {
//        TokenResponseDto tokenDto = authService.reissue(reissueRequestDto);
//        ResponseCookie cookie = tokenService.createAccessTokenCookie(tokenDto.getAccessToken());
//
//        return ResponseEntity.ok()
//            .header(HttpHeaders.SET_COOKIE, cookie.toString())
//            .body(new ApiResponse<>(
//                200,
//                tokenDto
//            ));
//    }
    @PostMapping("/reissue")
    public ApiResponse<TokenResponseDto> getSubscription(
        @RequestBody ReissueRequestDto reissueRequestDto
    ) throws JwtAuthenticationException {
        TokenResponseDto tokenDto = authService.reissue(reissueRequestDto);
        return new ApiResponse<>(
            200,
            tokenDto
        );
    }


    @PostMapping("/logout")
    public ApiResponse<String> logout(@AuthenticationPrincipal AuthUser authUser,
        HttpServletResponse response) {

        tokenService.clearTokenForUser(authUser.getId(), response);
        SecurityContextHolder.clearContext();

        return new ApiResponse<>(200, "로그아웃 완료");
    }

}
