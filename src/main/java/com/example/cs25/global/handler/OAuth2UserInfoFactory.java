package com.example.cs25.global.handler;

import com.example.cs25.domain.users.entity.SocialType;
import java.util.Map;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(SocialType socialType, Map<String, Object> attributes) {
        switch (socialType) {
            case GITHUB -> {
                return new GithubOAuth2UserInfo(attributes);
            }
            case KAKAO -> {
                return new KakaoOAuth2UserInfo(attributes);
            }

        }
        throw new OAuth2AuthenticationException("INVALID PROVIDER TYPE");
    }
}
