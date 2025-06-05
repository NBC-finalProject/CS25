package com.example.cs25.domain.oauth2.dto;


import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SocialType {
    KAKAO("kakao_account", "id", "email"),
    GITHUB(null, "id", "login"),
    NAVER("response", "id", "email");

    private final String attributeKey; //소셜로부터 전달받은 데이터를 Parsing하기 위해 필요한 key 값,
                                        // kakao는 kakao_account안에 필요한 정보들이 담겨져있음.
    private final String providerCode; // 각 소셜은 판별하는 판별 코드,
    private final String identifier;   // 소셜로그인을 한 사용자의 정보를 불러올 때 필요한 Key 값

    // 어떤 소셜로그인에 해당하는지 찾는 정적 메서드
    public static SocialType from(String provider) {
        String upperCastedProvider = provider.toUpperCase();

        return Arrays.stream(SocialType.values())
            .filter(item -> item.name().equals(upperCastedProvider))
            .findFirst()
            .orElseThrow();
    }
}
