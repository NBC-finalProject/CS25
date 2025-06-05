package com.example.cs25.domain.oauth2.dto;

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
		return (String) response.get("email");
	}

	@Override
	public String getName() {
		return (String) response.get("name");
	}
}