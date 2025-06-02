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
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
			return kakaoAccount.get("email").toString();
		} catch (Exception e){
			throw new IllegalStateException("카카오 계정정보에 이메일이 존재하지 않습니다.");
		}
	}

	@Override
	public String getName() {
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
			return properties.get("nickname").toString();
		} catch (Exception e){
			throw new IllegalStateException("카카오 계정정보에 닉네임이 존재하지 않습니다.");
		}
	}
}
