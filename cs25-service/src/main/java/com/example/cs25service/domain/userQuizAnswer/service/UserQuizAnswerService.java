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
import com.example.cs25service.domain.userQuizAnswer.dto.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserQuizAnswerService {

    private final UserQuizAnswerRepository userQuizAnswerRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final QuizCategoryRepository quizCategoryRepository;

    public Long answerSubmit(Long quizId, UserQuizAnswerRequestDto requestDto) {

        // 구독 정보 조회
        Subscription subscription = subscriptionRepository.findBySerialId(
                requestDto.getSubscriptionId())
            .orElseThrow(() -> new SubscriptionException(
                SubscriptionExceptionCode.NOT_FOUND_SUBSCRIPTION_ERROR));

        // 중복 답변 제출 막음
        boolean isDuplicate = userQuizAnswerRepository.existsByQuizIdAndSubscriptionId(quizId,
            subscription.getId());
        if (isDuplicate) {
            throw new UserQuizAnswerException(UserQuizAnswerExceptionCode.DUPLICATED_ANSWER);
        }

        // 유저 정보 조회
        User user = userRepository.findBySubscription(subscription).orElse(null);

        // 퀴즈 조회
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new QuizException(QuizExceptionCode.NOT_FOUND_ERROR));

        UserQuizAnswer answer = userQuizAnswerRepository.save(
            UserQuizAnswer.builder()
                .userAnswer(requestDto.getAnswer())
                .isCorrect(null)
                .user(user)
                .quiz(quiz)
                .subscription(subscription)
                .build()
        );
        return answer.getId();
    }

    /**
     * 객관식 or 주관식 채점
     * @param userQuizAnswerId
     * @return
     */
    @Transactional
    public CheckSimpleAnswerResponseDto checkSimpleAnswer(Long userQuizAnswerId) {
        UserQuizAnswer userQuizAnswer = userQuizAnswerRepository.findWithQuizAndUserById(userQuizAnswerId).orElseThrow(
                () -> new UserQuizAnswerException(UserQuizAnswerExceptionCode.NOT_FOUND_ANSWER)
        );

        Quiz quiz = userQuizAnswer.getQuiz();

        boolean isCorrect;
        if(quiz.getType().getScore() == 1){
            isCorrect = userQuizAnswer.getUserAnswer().equals(quiz.getAnswer().substring(0, 1));
        }else if(quiz.getType().getScore() == 3){
            isCorrect = userQuizAnswer.getUserAnswer().trim().equals(quiz.getAnswer().trim());
        }else{
            throw new QuizException(QuizExceptionCode.NOT_FOUND_ERROR);
        }

        User user = userQuizAnswer.getUser();
        // 회원인 경우에만 점수 부여
        if(user != null){
            double score;
            if(isCorrect){
                score = user.getScore() + (quiz.getType().getScore() * quiz.getLevel().getExp());
            }else{
                score = user.getScore() + 1;
            }
            user.updateScore(score);
        }

        userQuizAnswer.updateIsCorrect(isCorrect);

        return new CheckSimpleAnswerResponseDto(
                quiz.getQuestion(),
                userQuizAnswer.getUserAnswer(),
                quiz.getAnswer(),
                quiz.getCommentary(),
                userQuizAnswer.getIsCorrect()
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

}
