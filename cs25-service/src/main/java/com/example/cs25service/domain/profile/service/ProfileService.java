package com.example.cs25service.domain.profile.service;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.subscription.entity.SubscriptionHistory;
import com.example.cs25entity.domain.subscription.repository.SubscriptionHistoryRepository;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.exception.UserExceptionCode;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.profile.dto.ProfileResponseDto;
import com.example.cs25service.domain.profile.dto.ProfileWrongQuizResponseDto;
import com.example.cs25service.domain.profile.dto.UserSubscriptionResponseDto;
import com.example.cs25service.domain.profile.dto.WrongQuizDto;
import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.subscription.dto.SubscriptionHistoryDto;
import com.example.cs25service.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25service.domain.subscription.service.SubscriptionService;
import com.example.cs25service.domain.userQuizAnswer.dto.CategoryUserAnswerRateResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserQuizAnswerRepository userQuizAnswerRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final QuizCategoryRepository quizCategoryRepository;

    // 구독 정보 가져오기
    public UserSubscriptionResponseDto getUserSubscription(AuthUser authUser) {

        User user = userRepository.findBySerialId(authUser.getSerialId())
            .orElseThrow(() ->
                new UserException(UserExceptionCode.NOT_FOUND_USER));

        Long subscriptionId = user.getSubscription().getId();

        SubscriptionInfoDto subscriptionInfo = subscriptionService.getSubscription(
            user.getSubscription().getSerialId());

        //로그 다 모아와서 리스트로 만들기
        List<SubscriptionHistory> subLogs = subscriptionHistoryRepository
            .findAllBySubscriptionId(subscriptionId);
        List<SubscriptionHistoryDto> dtoList = subLogs.stream()
            .map(SubscriptionHistoryDto::fromEntity)
            .toList();

        return UserSubscriptionResponseDto.builder()
            .userId(user.getSerialId())
            .email(user.getEmail())
            .name(user.getName())
            .subscriptionLogPage(dtoList)
            .subscriptionInfoDto(subscriptionInfo)
            .build();
    }

    // 유저 틀린 문제 다시보기
    public ProfileWrongQuizResponseDto getWrongQuiz(AuthUser authUser) {

        User user = userRepository.findBySerialId(authUser.getSerialId())
            .orElseThrow(() ->
                new UserException(UserExceptionCode.NOT_FOUND_USER));

        List<WrongQuizDto> wrongQuizList = userQuizAnswerRepository
            // 유저 아이디로 내가 푼 문제 조회
            .findAllByUserId(user.getId()).stream()
            .filter(answer -> !answer.getIsCorrect()) // 틀린 문제
            .map(answer -> new WrongQuizDto(
                answer.getQuiz().getQuestion(),
                answer.getUserAnswer(),
                answer.getQuiz().getAnswer(),
                answer.getQuiz().getCommentary()
            ))
            .collect(Collectors.toList());

        return new ProfileWrongQuizResponseDto(authUser.getSerialId(), wrongQuizList);
    }

    public ProfileResponseDto getProfile(AuthUser authUser) {

        User user = userRepository.findBySerialId(authUser.getSerialId()).orElseThrow(
            () -> new UserException(UserExceptionCode.NOT_FOUND_USER)
        );

        // 랭킹
        int myRank = userRepository.findRankByScore(user.getScore());

        return ProfileResponseDto.builder()
            .name(user.getName())
            .rank(myRank)
            .score(user.getScore())
            .build();
    }

    //유저의 소분류 카테고리별 정답률 조회
    public CategoryUserAnswerRateResponse getUserQuizAnswerCorrectRate(AuthUser authUser) {

        //유저 검증
        User user = userRepository.findBySerialId(authUser.getSerialId()).orElseThrow(
            () -> new UserException(UserExceptionCode.NOT_FOUND_USER)
        );

        Long userId = user.getId();

        //유저 Id에 따른 구독 정보의 대분류 카테고리 조회
        QuizCategory parentCategory = quizCategoryRepository.findQuizCategoryByUserId(userId);

        //소분류 조회 -> getChildren()에서 실제 childCategories를 조회해오기 때문에 아래에서 이를 사용할 때 N+1 문제가 발생하지 않음
        List<QuizCategory> childCategories = parentCategory.getChildren();

        Map<String, Double> rates = new HashMap<>();
        //유저가 푼 문제들 중, 소분류에 속하는 로그 다 가져와
        for (QuizCategory child : childCategories) {
            List<UserQuizAnswer> answers = userQuizAnswerRepository.findByUserIdAndQuizCategoryId(
                userId, child.getId());

            if (answers.isEmpty()) {
                rates.put(child.getCategoryType(), 0.0);
                continue;
            }

            long totalAnswers = answers.size();
            long correctAnswers = answers.stream()
                .filter(UserQuizAnswer::getIsCorrect) // 정답인 경우 필터링
                .count();

            double answerRate = (double) correctAnswers / totalAnswers * 100;
            rates.put(child.getCategoryType(), answerRate);

        }

        return CategoryUserAnswerRateResponse.builder()
            .correctRates(rates)
            .build();
    }
}
