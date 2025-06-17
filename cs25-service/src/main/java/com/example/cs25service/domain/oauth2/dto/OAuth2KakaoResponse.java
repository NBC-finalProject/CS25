package com.example.cs25service.domain.oauth2.dto;

import com.example.cs25entity.domain.user.entity.SocialType;
import com.example.cs25service.domain.oauth2.exception.OAuth2Exception;
import com.example.cs25service.domain.oauth2.exception.OAuth2ExceptionCode;
import java.util.Map;

public class OAuth2KakaoResponse extends AbstractOAuth2Response {

    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> properties;

    public OAuth2KakaoResponse(Map<String, Object> attributes) {
        this.kakaoAccount = castOrThrow(attributes.get("kakao_account"));
        this.properties = castOrThrow(attributes.get("properties"));
    }

    @Override
    public SocialType getProvider() {
        return SocialType.KAKAO;
    }

    @Override
    public String getEmail() {
        try {
            return (String) kakaoAccount.get("email");
        } catch (Exception e) {
            throw new OAuth2Exception(OAuth2ExceptionCode.SOCIAL_EMAIL_NOT_FOUND);
        }
    }

    @Override
    public String getName() {
        try {
            return (String) properties.get("nickname");
        } catch (Exception e) {
            throw new OAuth2Exception(OAuth2ExceptionCode.SOCIAL_NAME_NOT_FOUND);
        }
    }
}
