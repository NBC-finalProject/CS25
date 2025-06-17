package com.example.cs25service.domain.oauth2.dto;


import com.example.cs25entity.domain.user.entity.SocialType;

public interface OAuth2Response {

    SocialType getProvider();

    String getEmail();

    String getName();
}
