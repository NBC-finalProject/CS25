package com.example.cs25.domain.oauth.dto;

import java.util.Map;

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
		@SuppressWarnings("unchecked")
		Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
		return kakaoAccount.get("email").toString();
	}

	@Override
	public String getName() {
		@SuppressWarnings("unchecked")
		Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
		return properties.get("nickname").toString();
	}
}
