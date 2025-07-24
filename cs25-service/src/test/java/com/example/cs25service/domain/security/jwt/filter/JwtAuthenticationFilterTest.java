package com.example.cs25service.domain.security.jwt.filter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.cs25entity.domain.user.entity.Role;
import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.security.filter.JwtAuthenticationFilter;
import com.example.cs25service.domain.security.jwt.exception.JwtAuthenticationException;
import com.example.cs25service.domain.security.jwt.exception.JwtExceptionCode;
import com.example.cs25service.domain.security.jwt.provider.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    JwtTokenProvider jwtTokenProvider;

    JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider);
    }

    @Nested
    @DisplayName("doFilterInternal 함수는 ")
    class inDoFilterInternal {

        @AfterEach
        void clearContext() {
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("정상 토큰일 경우 SecurityContext에 Authentication이 등록된다")
        void validToken_setsAuthentication() throws Exception {
            // given
            String token = "valid.jwt.token";
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            given(jwtTokenProvider.validateToken(token)).willReturn(true);
            given(jwtTokenProvider.getAuthorId(token)).willReturn("serial-user-123");
            given(jwtTokenProvider.getNickname(token)).willReturn("nickname");
            given(jwtTokenProvider.getRole(token)).willReturn(Role.USER);

            // when
            jwtAuthenticationFilter.doFilter(request, response, filterChain);

            // then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getPrincipal()).isInstanceOf(AuthUser.class);

            AuthUser authUser = (AuthUser) authentication.getPrincipal();
            assertThat(authUser.getSerialId()).isEqualTo("serial-user-123");
            assertThat(authUser.getName()).isEqualTo("nickname");
            assertThat(authUser.getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("토큰이 없으면 SecurityContext에 Authentication이 설정되지 않는다")
        void noToken_doesNotSetAuthentication() throws Exception {
            // given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            // when
            jwtAuthenticationFilter.doFilter(request, response, filterChain);

            // then
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("유효하지 않은 토큰이면 인증 실패 응답이 내려간다")
        void invalidToken_returnsErrorResponse() throws Exception {
            // given
            String token = "invalid.jwt.token";
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            given(jwtTokenProvider.validateToken(token))
                .willThrow(new JwtAuthenticationException(JwtExceptionCode.INVALID_TOKEN));

            // when
            jwtAuthenticationFilter.doFilter(request, response, filterChain);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            assertThat(response.getContentAsString()).contains("유효하지 않은 토큰입니다.");
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Nested
    @DisplayName("resolveToken 함수는")
    class inResolveToken {

        @Test
        @DisplayName("Authorization 헤더에 Bearer 토큰이 있으면 이를 반환한다")
        void resolvesTokenFromAuthorizationHeader() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer abc.def.ghi");

            String token = invokeResolveToken(request);
            assertThat(token).isEqualTo("abc.def.ghi");
        }

        @Test
        @DisplayName("Authorization 헤더가 없고 쿠키에 accessToken이 있으면 이를 반환한다")
        void resolvesTokenFromCookie() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            Cookie cookie = new Cookie("accessToken", "cookie.jwt.token");
            request.setCookies(cookie);

            String token = invokeResolveToken(request);
            assertThat(token).isEqualTo("cookie.jwt.token");
        }

        @Test
        @DisplayName("Authorization과 쿠키 모두 없으면 null 반환")
        void returnsNullIfNoToken() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            String token = invokeResolveToken(request);
            assertThat(token).isNull();
        }

        //리플렉션으로 private method 호출
        private String invokeResolveToken(HttpServletRequest request) {
            try {
                Method method = JwtAuthenticationFilter.class.getDeclaredMethod("resolveToken",
                    HttpServletRequest.class);
                method.setAccessible(true);
                return (String) method.invoke(jwtAuthenticationFilter, request);
            } catch (Exception e) {
                throw new RuntimeException("resolveToken 호출 실패", e);
            }
        }
    }

}