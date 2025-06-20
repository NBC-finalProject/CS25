package com.example.cs25service.domain.profile.service;

import com.example.cs25entity.domain.subscription.entity.SubscriptionHistory;
import com.example.cs25entity.domain.subscription.repository.SubscriptionHistoryRepository;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.exception.UserExceptionCode;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.profile.dto.ProfileResponseDto;
import com.example.cs25service.domain.profile.dto.ProfileWrongQuizResponseDto;
import com.example.cs25service.domain.profile.dto.UserSubscriptionResponseDto;
import com.example.cs25service.domain.profile.dto.WrongQuizDto;
import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.subscription.dto.SubscriptionHistoryDto;
import com.example.cs25service.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25service.domain.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserQuizAnswerRepository userQuizAnswerRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;

    // 구독 정보 가져오기
    public UserSubscriptionResponseDto getUserSubscription(AuthUser authUser) {

        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() ->
                        new UserException(UserExceptionCode.NOT_FOUND_USER));

        Long subscriptionId = user.getSubscription().getId();

        SubscriptionInfoDto subscriptionInfo = subscriptionService.getSubscription(
                subscriptionId);

        //로그 다 모아와서 리스트로 만들기
        List<SubscriptionHistory> subLogs = subscriptionHistoryRepository
                .findAllBySubscriptionId(subscriptionId);
        List<SubscriptionHistoryDto> dtoList = subLogs.stream()
                .map(SubscriptionHistoryDto::fromEntity)
                .toList();

        return UserSubscriptionResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .subscriptionLogPage(dtoList)
                .subscriptionInfoDto(subscriptionInfo)
                .build();
    }

    // 유저 틀린 문제 다시보기
    public ProfileWrongQuizResponseDto getWrongQuiz(AuthUser authUser) {

        List<WrongQuizDto> wrongQuizList = userQuizAnswerRepository
                // 유저 아이디로 내가 푼 문제 조회
                .findAllByUserId(authUser.getId()).stream()
                .filter(answer -> !answer.getIsCorrect()) // 틀린 문제
                .map(answer -> new WrongQuizDto(
                        answer.getQuiz().getQuestion(),
                        answer.getUserAnswer(),
                        answer.getQuiz().getAnswer(),
                        answer.getQuiz().getCommentary()
                ))
                .collect(Collectors.toList());

        return new ProfileWrongQuizResponseDto(authUser.getId(), wrongQuizList);
    }

    public ProfileResponseDto getProfile(AuthUser authUser) {

        User user = userRepository.findById(authUser.getId()).orElseThrow(
                () -> new UserException(UserExceptionCode.NOT_FOUND_USER)
        );

        // 랭킹
        int myRank = userRepository.findRankByScore(user.getScore());

        return new ProfileResponseDto(
                user.getName(),
                user.getScore(),
                myRank
        );
    }
}
