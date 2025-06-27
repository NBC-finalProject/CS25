package com.example.cs25.domain.oauth2.dto;

public interface OAuth2Response {
	SocialType getProvider();

	String getEmail();

	String getName();
}
