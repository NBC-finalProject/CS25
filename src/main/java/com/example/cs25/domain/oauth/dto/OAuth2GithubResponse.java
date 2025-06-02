package com.example.cs25.domain.oauth.dto;

import java.util.Map;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OAuth2GithubResponse implements OAuth2Response{
	private final Map<String, Object> attributes;

	@Override
	public SocialType getProvider() {
		return SocialType.GITHUB;
	}

	@Override
	public String getEmail() {
		return (String) attributes.get("email");
	}

	@Override
	public String getName() {
		String name = (String) attributes.get("name");
		return name != null ? name : (String) attributes.get("login");
	}
}
