package com.example.cs25service.domain.userQuizAnswer.service;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.exception.SubscriptionException;
import com.example.cs25entity.domain.subscription.exception.SubscriptionExceptionCode;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.exception.UserExceptionCode;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25entity.domain.userQuizAnswer.dto.UserAnswerDto;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.exception.UserQuizAnswerException;
import com.example.cs25entity.domain.userQuizAnswer.exception.UserQuizAnswerExceptionCode;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.userQuizAnswer.dto.CategoryUserAnswerRateResponse;
import com.example.cs25service.domain.userQuizAnswer.dto.SelectionRateResponseDto;
import com.example.cs25service.domain.userQuizAnswer.dto.UserQuizAnswerRequestDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQuizAnswerService {

    private final UserQuizAnswerRepository userQuizAnswerRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final QuizCategoryRepository quizCategoryRepository;

    public void answerSubmit(Long quizId, UserQuizAnswerRequestDto requestDto) {
        // 중복 답변 제출 막음
        boolean isDuplicate = userQuizAnswerRepository.existsByQuizIdAndSubscriptionId(quizId, requestDto.getSubscriptionId());
        if (isDuplicate) {
            throw new UserQuizAnswerException(UserQuizAnswerExceptionCode.DUPLICATED_ANSWER);
        }

        // 구독 정보 조회
        Subscription subscription = subscriptionRepository.findById(requestDto.getSubscriptionId())
            .orElseThrow(() -> new SubscriptionException(
                SubscriptionExceptionCode.NOT_FOUND_SUBSCRIPTION_ERROR));

        // 유저 정보 조회
        User user = userRepository.findBySubscription(subscription);

        // 퀴즈 조회
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new QuizException(QuizExceptionCode.NOT_FOUND_ERROR));

        // 정답 체크
        boolean isCorrect = requestDto.getAnswer().equals(quiz.getAnswer().substring(0, 1));

        userQuizAnswerRepository.save(
            UserQuizAnswer.builder()
                .userAnswer(requestDto.getAnswer())
                .isCorrect(isCorrect)
                .user(user)
                .quiz(quiz)
                .subscription(subscription)
                .build()
        );
    }

    public SelectionRateResponseDto getSelectionRateByOption(Long quizId) {
        List<UserAnswerDto> answers = userQuizAnswerRepository.findUserAnswerByQuizId(quizId);

        //보기별 선택 수 집계
        Map<String, Long> counts = answers.stream()
            .map(UserAnswerDto::getUserAnswer)
            .filter(Objects::nonNull)
            .map(String::trim)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 총 응답 수 계산
        long total = counts.values().stream().mapToLong(Long::longValue).sum();

        // 선택률 계산
        Map<String, Double> rates = counts.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> (double) e.getValue() / total
            ));

        return new SelectionRateResponseDto(rates, total);
    }

    public CategoryUserAnswerRateResponse getUserQuizAnswerCorrectRate(Long userId){
        //유저 검증
        User user = userRepository.findByIdOrElseThrow(userId);
        if(!user.isActive()){
            throw new UserException(UserExceptionCode.INACTIVE_USER);
        }

        //유저 Id에 따른 구독 정보의 대분류 카테고리 조회
        QuizCategory parentCategory = quizCategoryRepository.findQuizCategoryByUserId(userId);

        //소분류 조회
        List<QuizCategory> childCategories = parentCategory.getChildren();

        Map<String, Double> rates = new HashMap<>();
        //유저가 푼 문제들 중, 소분류에 속하는 로그 다 가져와
        for(QuizCategory child : childCategories){
            List<UserQuizAnswer> answers = userQuizAnswerRepository.findByUserIdAndQuizCategoryId(userId, child.getId());

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
