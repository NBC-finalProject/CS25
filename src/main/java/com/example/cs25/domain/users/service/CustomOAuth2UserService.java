package com.example.cs25.domain.users.service;

import com.example.cs25.domain.users.dto.KakaoUserInfoResponse;
import com.example.cs25.domain.users.entity.AuthUser;
import com.example.cs25.domain.users.entity.SocialType;
import com.example.cs25.domain.users.entity.User;
import com.example.cs25.domain.users.exception.UserException;
import com.example.cs25.domain.users.exception.UserExceptionCode;
import com.example.cs25.domain.users.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        User loginUser = new User();

        if(registrationId.equals("kakao")){
            KakaoUserInfoResponse kakaoUser = objectMapper.convertValue(attributes, KakaoUserInfoResponse.class);
            String kakaoEmail = kakaoUser.getEmail();
            String kakaoNickname = kakaoUser.getNickname();

            //회원인지 확인
            userRepository.validateSocialJoinEmail(kakaoEmail, SocialType.KAKAO);

            loginUser = userRepository.findByEmail(kakaoEmail).orElseGet(() ->
                userRepository.save(User.builder()
                    .email(kakaoEmail)
                    .name(kakaoNickname)
                    .socialType(SocialType.KAKAO)
                    .build()));
        } else if(registrationId.equals("github")) {

        } else{
            throw new UserException(UserExceptionCode.UNSUPPORTED_SOCIAL_PROVIDER);
        }

        return new AuthUser(loginUser.getId(), loginUser.getEmail(), loginUser.getName(), loginUser.getRole());
    }
}
