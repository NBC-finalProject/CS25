package com.example.cs25service.domain.users.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.security.jwt.dto.ReissueRequestDto;
import com.example.cs25service.domain.security.jwt.dto.TokenResponseDto;
import com.example.cs25service.domain.security.jwt.service.TokenService;
import com.example.cs25service.domain.users.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    AuthUser mockUser = mock(AuthUser.class);

    @Nested
    @DisplayName("POST /auth/reissue")
    @WithMockUser(username = "tofha")
    class Reissue {

        @Test
        @DisplayName("토큰 재발급 요청에 성공하면 200과 새 토큰을 반환한다")
        void reissue_success() throws Exception {
            // given
            ReissueRequestDto request = new ReissueRequestDto("oldRefreshToken");
            TokenResponseDto response = new TokenResponseDto("newAccessToken", "newRefreshToken");

            given(authService.reissue(any())).willReturn(response);
            given(tokenService.createAccessTokenCookie(anyString()))
                .willReturn(ResponseCookie.from("accessToken", "newAccessToken").build());

            // when & then
            mockMvc.perform(post("/auth/reissue")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("newAccessToken"))
                .andExpect(jsonPath("$.data.refreshToken").value("newRefreshToken"));
        }
    }

    @Nested
    @DisplayName("GET /auth/status")
    @WithMockUser(username = "tofha")
    class Status {

        @Test
        @DisplayName("로그인 상태일 경우 true 반환")
        void loginStatus_authenticated() throws Exception {

            Authentication auth = new UsernamePasswordAuthenticationToken(
                mockUser,  // ← principal 자리에 반드시 AuthUser 직접 넣기
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(get("/auth/status")
                    .with(authentication(auth))
                    //.with(authentication(new TestingAuthenticationToken(mockUser, null)))
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("비로그인 상태일 경우 false 반환")
        void loginStatus_unauthenticated() throws Exception {
            mockMvc.perform(get("/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
        }
    }

    @Nested
    @DisplayName("POST /auth/logout")
    @WithMockUser(username = "tofha")
    class Logout {

        @Test
        @DisplayName("정상 로그아웃 시 200과 완료 메시지 반환")
        void logout_success() throws Exception {

            Authentication auth = new UsernamePasswordAuthenticationToken(
                mockUser,  // ← principal 자리에 반드시 AuthUser 직접 넣기
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );

            doNothing().when(tokenService).clearTokenForUser(anyString(), any());
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(post("/auth/logout")
                    .with(authentication(auth))
                    //.with(authentication(new TestingAuthenticationToken(mockUser, null)))
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("로그아웃 완료"));

            SecurityContextHolder.clearContext();
        }
    }
}
