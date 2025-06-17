package com.example.cs25service.domain.oauth2.dto;

import com.example.cs25entity.domain.user.entity.SocialType;
import com.example.cs25service.domain.oauth2.exception.OAuth2Exception;
import com.example.cs25service.domain.oauth2.exception.OAuth2ExceptionCode;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
public class OAuth2GithubResponse extends AbstractOAuth2Response {

    private final Map<String, Object> attributes;
    private final String accessToken;

    @Override
    public SocialType getProvider() {
        return SocialType.GITHUB;
    }

    @Override
    public String getEmail() {
        try {
            String attributeEmail = (String) attributes.get("email");
            return attributeEmail != null ? attributeEmail : fetchEmailWithAccessToken(accessToken);
        } catch (Exception e) {
            throw new OAuth2Exception(OAuth2ExceptionCode.SOCIAL_EMAIL_NOT_FOUND);
        }
    }

    @Override
    public String getName() {
        try {
            String name = (String) attributes.get("name");
            return name != null ? name : (String) attributes.get("login");
        } catch (Exception e) {
            throw new OAuth2Exception(OAuth2ExceptionCode.SOCIAL_NAME_NOT_FOUND);
        }
    }

    /**
     * public 이메일이 없을 경우, accessToken을 사용하여 이메일을 반환하는 메서드
     *
     * @param accessToken 사용자 액세스 토큰
     * @return private 사용자 이메일을 반환
     */
    private String fetchEmailWithAccessToken(String accessToken) {
        WebClient webClient = WebClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json")
            .build();

        List<Map<String, Object>> emails = webClient.get()
            .uri("/user/emails")
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
            })
            .block();

        if (emails != null) {
            for (Map<String, Object> emailEntry : emails) {
                if (Boolean.TRUE.equals(emailEntry.get("primary")) && Boolean.TRUE.equals(
                    emailEntry.get("verified"))) {
                    return (String) emailEntry.get("email");
                }
            }
        }
        throw new OAuth2Exception(OAuth2ExceptionCode.SOCIAL_EMAIL_NOT_FOUND_WITH_TOKEN);
    }
}
