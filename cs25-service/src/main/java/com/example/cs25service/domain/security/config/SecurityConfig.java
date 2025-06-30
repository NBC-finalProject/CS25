package com.example.cs25service.domain.security.config;

import com.example.cs25common.global.exception.ErrorResponseUtil;
import com.example.cs25service.domain.oauth2.handler.OAuth2LoginSuccessHandler;
import com.example.cs25service.domain.oauth2.service.CustomOAuth2UserService;
import com.example.cs25service.domain.security.jwt.filter.JwtAuthenticationFilter;
import com.example.cs25service.domain.security.jwt.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PERMITTED_ROLES = {"USER", "ADMIN"};
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Value("${FRONT_END_URI:http://localhost:5173}")
    private String frontEndUri;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin(frontEndUri);
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
        CustomOAuth2UserService customOAuth2UserService) throws Exception {
        return http

            .httpBasic(HttpBasicConfigurer::disable)

            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 모든 요청에 대해 보안 정책을 적용함 (securityMatcher 선택적)
            .securityMatcher((request -> true))

            // CSRF 보호 비활성화 (JWT 세션을 사용하지 않기 때문에 필요 없음)
            .csrf(AbstractHttpConfigurer::disable)

            // OAuth 사용으로 인해 기본 로그인 비활성화
            .formLogin(FormLoginConfigurer::disable)

            // 세션 사용 안함 (STATELESS)
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(request -> request

                //로그인이 필요한 서비스만 여기다가 추가하기 (permaiAll 은 패싱 ㄱㄱ)
                .requestMatchers(HttpMethod.GET, "/users/**").hasAnyRole(PERMITTED_ROLES)
                .requestMatchers(HttpMethod.POST, "/quizzes/upload/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/auth/**").hasAnyRole(PERMITTED_ROLES)
                //.requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/admin/quiz-categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/crawlers/github/**").hasRole("ADMIN")

                .anyRequest().permitAll()
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    ErrorResponseUtil.writeJsonError(response, 401,
                        "사용자 인증이 필요한 요청입니다.");
                    //response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증되지 않은 사용자입니다.");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    ErrorResponseUtil.writeJsonError(response, 403, "접근 권한이 없습니다.");
                    //response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다.");
                })
            )

            .oauth2Login(oauth2 -> oauth2
                    .loginPage("/login")
                    .successHandler(oAuth2LoginSuccessHandler)
                    .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                        .userService(customOAuth2UserService)
                    )
                //.defaultSuccessUrl("/home", true) // 로그인 성공 후 이동할 URL
            )

            // JWT 인증 필터 등록
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class)

            // 최종 SecurityFilterChain 반환
            .build();
    }
}
