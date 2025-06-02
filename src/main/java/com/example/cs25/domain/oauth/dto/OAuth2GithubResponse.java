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
		try {
			return (String) attributes.get("email");
		} catch (Exception e){
			throw new IllegalStateException("깃허브 계정정보에 이메일이 존재하지 않습니다.");
		}

	}

	@Override
	public String getName() {
		try {
			String name = (String) attributes.get("name");
			return name != null ? name : (String) attributes.get("login");
		} catch (Exception e){
			throw new IllegalStateException("깃허브 계정정보에 이름이 존재하지 않습니다.");
		}
	}
}
