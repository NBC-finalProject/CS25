package com.example.cs25.global.config;

import com.example.cs25.domain.users.service.CustomOAuth2UserService;
import com.example.cs25.global.handler.OAuth2LoginSuccessHandler;
import com.example.cs25.global.jwt.filter.JwtAuthenticationFilter;
import com.example.cs25.global.jwt.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String PERMITTED_ROLES[] = {"USER", "ADMIN"};
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService) throws Exception {
        return http

            .httpBasic(HttpBasicConfigurer::disable)
            // 모든 요청에 대해 보안 정책을 적용함 (securityMatcher 선택적)
            .securityMatcher((request -> true))

            // CSRF 보호 비활성화 (JWT 세션을 사용하지 않기 때문에 필요 없음)
            .csrf(AbstractHttpConfigurer::disable)

            .formLogin(FormLoginConfigurer::disable)

            // 세션 사용 안함 (STATELESS)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(request -> request
                .requestMatchers("/oauth2/**", "/login/oauth2/code/**").permitAll()
                .anyRequest().hasAnyRole(PERMITTED_ROLES)
            )


            .oauth2Login(oauth2 -> oauth2
                // .loginPage("/login")
                .successHandler(oAuth2LoginSuccessHandler)
                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                    .userService(customOAuth2UserService)
                )
                //.defaultSuccessUrl("/home", true) // 로그인 성공 후 이동할 URL
            )

            // JWT 인증 필터 등록
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)


            // 최종 SecurityFilterChain 반환
            .build();
    }
}
