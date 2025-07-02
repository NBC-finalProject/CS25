package com.example.cs25service.domain.users.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.security.jwt.provider.JwtTokenProvider;
import com.example.cs25service.domain.users.service.UserService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ResponseStatus;

@ActiveProfiles("test")
@WebMvcTest(UserController.class)
//@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터 제거 (권한 무시)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("유저 탈퇴 요청 성공 시 204 반환")
    @WithMockUser(username = "tofha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteUser_success() throws Exception {
        // given
        AuthUser mockUser = mock(AuthUser.class);
        Authentication auth = new UsernamePasswordAuthenticationToken(
            mockUser,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        willDoNothing().given(userService).disableUser(any(AuthUser.class));

        // when & then
        mockMvc.perform(patch("/users")
                .with(authentication(auth)) // 인증 주입
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andDo(print())
            .andExpect(jsonPath("$.httpCode").value(204));

        verify(userService).disableUser(mockUser);
    }
}

