package com.example.cs25.domain.users.service;

import com.example.cs25.domain.oauth.dto.OAuth2GithubResponse;
import com.example.cs25.domain.oauth.dto.OAuth2KakaoResponse;
import com.example.cs25.domain.oauth.dto.OAuth2Response;
import com.example.cs25.domain.oauth.dto.SocialType;
import com.example.cs25.domain.users.entity.Role;
import com.example.cs25.domain.users.entity.User;
import com.example.cs25.domain.users.exception.UserException;
import com.example.cs25.domain.users.exception.UserExceptionCode;
import com.example.cs25.domain.users.repository.UserRepository;
import com.example.cs25.global.dto.AuthUser;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 서비스를 구분하는 아이디 ex) Kakao, Github ...
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        SocialType socialType = SocialType.from(registrationId);

        // 서비스에서 제공받은 데이터
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2Response oAuth2Response = getOAuth2Response(socialType, attributes);
        userRepository.validateSocialJoinEmail(oAuth2Response.getEmail(), socialType);

        User loginUser = getUser(oAuth2Response);
        return new AuthUser(loginUser);
    }

    /**
     * 제공자에 따라 OAuth2 응답객체를 생성하는 메서드
     *
     * @param socialType 서비스 제공자 (Kakao, Github ...)
     * @param attributes 제공받은 데이터
     * @return OAuth2 응답객체를 반환
     * @throws UserException 지원하지 않는 서비스 제공자일 경우 예외처리
     */
    private OAuth2Response getOAuth2Response(SocialType socialType,
        Map<String, Object> attributes) {
        if (socialType == SocialType.KAKAO) {
            return new OAuth2KakaoResponse(attributes);
        } else if (socialType == SocialType.GITHUB) {
            return new OAuth2GithubResponse(attributes);
        } else {
            throw new UserException(UserExceptionCode.UNSUPPORTED_SOCIAL_PROVIDER);
        }
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
            throw new UserException(UserExceptionCode.OAUTH2_PROFILE_INCOMPLETE);
        }

        return userRepository.findByEmail(email).orElseGet(() ->
            userRepository.save(User.builder()
                .email(email)
                .name(name)
                .socialType(provider)
                .role(Role.USER)
                .build()));
    }
}
