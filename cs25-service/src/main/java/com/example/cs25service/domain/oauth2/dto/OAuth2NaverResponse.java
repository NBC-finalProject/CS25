package com.example.cs25service.domain.oauth2.dto;

import com.example.cs25entity.domain.user.entity.SocialType;
import com.example.cs25service.domain.oauth2.exception.OAuth2Exception;
import com.example.cs25service.domain.oauth2.exception.OAuth2ExceptionCode;
import java.util.Map;

public class OAuth2NaverResponse extends AbstractOAuth2Response {

    private final Map<String, Object> response;

    public OAuth2NaverResponse(Map<String, Object> attributes) {
        this.response = castOrThrow(attributes.get("response"));
    }

    @Override
    public SocialType getProvider() {
        return SocialType.NAVER;
    }

    @Override
    public String getEmail() {
        try {
            return (String) response.get("email");
        } catch (Exception e) {
            throw new OAuth2Exception(OAuth2ExceptionCode.SOCIAL_EMAIL_NOT_FOUND);
        }
    }

    @Override
    public String getName() {
        try {
            return (String) response.get("name");
        } catch (Exception e) {
            throw new OAuth2Exception(OAuth2ExceptionCode.SOCIAL_NAME_NOT_FOUND);
        }
    }
}