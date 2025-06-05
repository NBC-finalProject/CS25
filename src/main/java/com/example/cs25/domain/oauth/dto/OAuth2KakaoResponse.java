package com.example.cs25.domain.oauth.dto;

import java.util.Map;

import com.example.cs25.domain.oauth.exception.OAuth2Exception;
import com.example.cs25.domain.oauth.exception.OAuth2ExceptionCode;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OAuth2KakaoResponse implements OAuth2Response{
	private final Map<String, Object> attributes;

	@Override
	public SocialType getProvider() {
		return SocialType.KAKAO;
	}

	@Override
	public String getEmail() {
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
			return kakaoAccount.get("email").toString();
		} catch (Exception e){
			throw new OAuth2Exception(OAuth2ExceptionCode.SOCIAL_EMAIL_NOT_FOUND);
		}
	}

	@Override
	public String getName() {
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
			return properties.get("nickname").toString();
		} catch (Exception e){
			throw new OAuth2Exception(OAuth2ExceptionCode.SOCIAL_NAME_NOT_FOUND);
		}
	}
}
