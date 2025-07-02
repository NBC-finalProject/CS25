package com.example.cs25service.domain.oauth2.service;

import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.user.entity.Role;
import com.example.cs25entity.domain.user.entity.SocialType;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25service.domain.oauth2.dto.OAuth2GithubResponse;
import com.example.cs25service.domain.oauth2.dto.OAuth2KakaoResponse;
import com.example.cs25service.domain.oauth2.dto.OAuth2NaverResponse;
import com.example.cs25service.domain.oauth2.dto.OAuth2Response;
import com.example.cs25service.domain.oauth2.exception.OAuth2Exception;
import com.example.cs25service.domain.oauth2.exception.OAuth2ExceptionCode;
import com.example.cs25service.domain.security.dto.AuthUser;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final EntityManagerFactoryInfo entityManagerFactoryInfo;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 서비스를 구분하는 아이디 ex) Kakao, Github ...
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        SocialType socialType = SocialType.from(registrationId);
        String accessToken = userRequest.getAccessToken().getTokenValue();

        // 서비스에서 제공받은 데이터
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2Response oAuth2Response = getOAuth2Response(socialType, attributes, accessToken);
        userRepository.validateSocialJoinEmail(oAuth2Response.getEmail(), socialType);

        User loginUser = getUser(oAuth2Response);
        return new AuthUser(loginUser);
    }

    /**
     * 제공자에 따라 OAuth2 응답객체를 생성하는 메서드
     *
     * @param socialType  서비스 제공자 (Kakao, Github ...)
     * @param attributes  제공받은 데이터
     * @param accessToken 액세스토큰 (Github 이메일 찾는데 사용)
     * @return OAuth2 응답객체를 반환
     */
    private OAuth2Response getOAuth2Response(SocialType socialType, Map<String, Object> attributes,
        String accessToken) {
        if(socialType == null){
            throw new OAuth2Exception(OAuth2ExceptionCode.SOCIAL_PROVIDER_NOT_FOUND);
        }
        return switch (socialType) {
            case KAKAO -> new OAuth2KakaoResponse(attributes);
            case GITHUB -> new OAuth2GithubResponse(attributes, accessToken);
            case NAVER -> new OAuth2NaverResponse(attributes);
            default -> throw new OAuth2Exception(OAuth2ExceptionCode.SOCIAL_PROVIDER_UNSUPPORTED);
        };
    }

    /**
     * OAuth2 응답객체를 갖고 기존 사용자 조회하거나 없을 경우 생성하는 메서드
     *
     * @param oAuth2Response OAuth2 응답 객체
     * @return 유저 엔티티를 반환
     */
    private User getUser(OAuth2Response oAuth2Response) {
        String email = oAuth2Response.getEmail();
        String name = oAuth2Response.getName();
        SocialType provider = oAuth2Response.getProvider();

        if (email == null || name == null || provider == null) {
            throw new OAuth2Exception(OAuth2ExceptionCode.SOCIAL_REQUIRED_FIELDS_MISSING);
        }

        // 기존 User 조회
        User existingUser = userRepository.findByEmail(email).orElse(null);

        // 기존 유저가 있다면, isActive 값 확인 후 true로 업데이트
        if (existingUser != null) {
            if (!existingUser.isActive()) {
                existingUser.updateEnableUser();  // isActive를 true로 설정
                userRepository.save(existingUser);  // 변경 사항 저장
            }
            return existingUser;
        }

        Subscription subscription = subscriptionRepository.findByEmail(email).orElse(null);
        return userRepository.save(User.builder()
            .email(email)
            .name(name)
            .socialType(provider)
            .role(Role.USER) // 새로운 유저는 기본적으로 isActive=true
            .subscription(subscription)
            .build());
    }
}
