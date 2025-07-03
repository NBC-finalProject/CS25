package com.example.cs25service.domain.userQuizAnswer.service;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
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
import com.example.cs25service.domain.userQuizAnswer.dto.SelectionRateResponseDto;
import com.example.cs25service.domain.userQuizAnswer.dto.UserQuizAnswerRequestDto;
import com.example.cs25service.domain.userQuizAnswer.dto.UserQuizAnswerResponseDto;
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

    /**
     * 사용자의 퀴즈 답변을 저장하는 메서드
     * 중복 답변을 방지하고 사용자 정보와 함께 답변을 저장
     *
     * @param quizSerialId 퀴즈 시리얼 ID (UUID)
     * @param requestDto   사용자 답변 요청 DTO
     * @return 저장된 사용자 퀴즈 답변의 ID
     * @throws SubscriptionException   구독 정보를 찾을 수 없는 경우
     * @throws QuizException           퀴즈를 찾을 수 없는 경우
     * @throws UserQuizAnswerException 중복 답변인 경우
     */
    @Transactional
    public UserQuizAnswerResponseDto submitAnswer(String quizSerialId, UserQuizAnswerRequestDto requestDto) {

        Subscription subscription = subscriptionRepository.findBySerialIdOrElseThrow(
            requestDto.getSubscriptionId());

        // 구독 활성화 상태인지 조회
        if (!subscription.isActive()) {
            throw new SubscriptionException(SubscriptionExceptionCode.DISABLED_SUBSCRIPTION_ERROR);
        }

        Quiz quiz = quizRepository.findBySerialIdOrElseThrow(quizSerialId);

        // 이미 답변했는지 여부 조회
        boolean isDuplicate = userQuizAnswerRepository
            .existsByQuizIdAndSubscriptionId(quiz.getId(), subscription.getId());

        // 이미 답변했으면
        if(isDuplicate) {
            UserQuizAnswer userQuizAnswer = userQuizAnswerRepository
                .findUserQuizAnswerBySerialIds(quizSerialId, requestDto.getSubscriptionId());

            // 유효한 답변객체인지 검증
            validateDuplicatedUserAnswer(userQuizAnswer);

            // 서술형 답변인지 확인
            boolean isSubjectiveAnswer = getSubjectiveAnswerStatus(userQuizAnswer, quiz);

            return toAnswerDto(userQuizAnswer, quiz, true, isSubjectiveAnswer);
        }
        // 처음 답변한 경우 답변 생성하여 저장
        else {
            User user = userRepository.findBySubscription(subscription).orElse(null);

            // 서술형의 경우는 AiFeedbackStreamProcesser 로직에서 isCorrect, aiFeedback 컬럼을 저장
            UserQuizAnswer savedUserQuizAnswer = userQuizAnswerRepository.save(
                UserQuizAnswer.builder()
                    .userAnswer(requestDto.getAnswer())
                    .isCorrect(null)
                    .user(user)
                    .quiz(quiz)
                    .subscription(subscription)
                    .build()
            );
            return toAnswerDto(savedUserQuizAnswer, quiz, false, false);
        }
    }

    /**
     * 사용자의 퀴즈 답변을 채점하고 결과를 반환하는 메서드
     * 객관식과 주관식 문제를 모두 지원하며, 회원인 경우 점수를 업데이트
     * 
     * @param userQuizAnswerId 사용자 퀴즈 답변 ID
     * @return 채점 결과를 포함한 응답 DTO
     * @throws UserQuizAnswerException 답변을 찾을 수 없는 경우
     */
    @Transactional
    public UserQuizAnswerResponseDto evaluateAnswer(Long userQuizAnswerId) {
        UserQuizAnswer userQuizAnswer = userQuizAnswerRepository
            .findWithQuizAndUserByIdOrElseThrow(userQuizAnswerId);
        Quiz quiz = userQuizAnswer.getQuiz();

        // 정답인지 채점하고 업데이트
        boolean isAnswerCorrect = getAnswerCorrectStatus(quiz, userQuizAnswer);
        userQuizAnswer.updateIsCorrect(isAnswerCorrect);

        return UserQuizAnswerResponseDto.builder()
            .userQuizAnswerId(userQuizAnswerId)
            .question(quiz.getQuestion())
            .answer(quiz.getAnswer())
            .commentary(quiz.getCommentary())
            .isCorrect(userQuizAnswer.getIsCorrect())
            .build();
    }

    /**
     * 특정 퀴즈의 각 선택지별 선택률을 계산하는 메서드
     * 모든 사용자의 답변을 집계하여 통계 정보를 반환
     * 
     * @param quizSerialId 퀴즈 시리얼 ID
     * @return 선택지별 선택률과 총 응답 수를 포함한 응답 DTO
     * @throws QuizException 퀴즈를 찾을 수 없는 경우
     */
    public SelectionRateResponseDto calculateSelectionRateByOption(String quizSerialId) {
        Quiz quiz = quizRepository.findBySerialIdOrElseThrow(quizSerialId);
        List<UserAnswerDto> answers = userQuizAnswerRepository.findUserAnswerByQuizId(quiz.getId());

        //보기별 선택 수 집계
        Map<String, Long> selectionCounts = answers.stream()
            .map(UserAnswerDto::getUserAnswer)
            .filter(Objects::nonNull)
            .map(String::trim)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 총 응답 수 계산
        long totalResponses = selectionCounts.values().stream().mapToLong(Long::longValue).sum();

        // 선택률 계산
        Map<String, Double> selectionRates = selectionCounts.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> (double) entry.getValue() / totalResponses
            ));

        return new SelectionRateResponseDto(selectionRates, totalResponses);
    }

    /**
     * 답변 DTO를 생성하여 반환하는 메서드
     * @param userQuizAnswer 답변 객체
     * @param quiz 문제 객체
     * @param isDuplicate 중복 여부
     * @return 답변 DTO를 반환
     */
    private UserQuizAnswerResponseDto toAnswerDto (
        UserQuizAnswer userQuizAnswer,
        Quiz quiz,
        boolean isDuplicate,
        boolean isSubjectiveAnswer
    ) {
        return UserQuizAnswerResponseDto.builder()
            .userQuizAnswerId(userQuizAnswer.getId())
            .question(quiz.getQuestion())
            .commentary(quiz.getCommentary())
            .userAnswer(userQuizAnswer.getUserAnswer())
            .answer(quiz.getAnswer())
            .isCorrect(userQuizAnswer.getIsCorrect())
            .duplicated(isDuplicate)
            .aiFeedback(isSubjectiveAnswer ? userQuizAnswer.getAiFeedback() : null)
            .build();
    }

    /**
     * 사용자의 답변이 정답인지 확인하고 점수를 업데이트하는 메서드
     * 채점 로직을 실행한 후 회원인 경우 점수를 업데이트
     * 
     * @param quiz 퀴즈 정보
     * @param userQuizAnswer 사용자 답변 정보
     * @return 답변 정답 여부
     * @throws QuizException 지원하지 않는 퀴즈 타입인 경우
     */
    private boolean getAnswerCorrectStatus(Quiz quiz, UserQuizAnswer userQuizAnswer) {
        boolean isAnswerCorrect = checkAnswer(quiz, userQuizAnswer);
        updateUserScore(userQuizAnswer.getUser(), quiz, isAnswerCorrect);
        return isAnswerCorrect;
    }

    /**
     * 퀴즈 타입에 따라 사용자 답변의 정답 여부를 채점하는 메서드
     * - 객관식/주관식 (score=1,3): 사용자 답변과 정답을 공백 제거하여 비교
     * 
     * @param quiz 퀴즈 정보
     * @param userQuizAnswer 사용자 답변 정보
     * @return 답변 정답 여부 (true: 정답, false: 오답)
     * @throws QuizException 지원하지 않는 퀴즈 타입인 경우
     */
    private boolean checkAnswer(Quiz quiz, UserQuizAnswer userQuizAnswer) {
        if(quiz.getType().getScore() == 1 || quiz.getType().getScore() == 3){
            return userQuizAnswer.getUserAnswer().trim().equals(quiz.getAnswer().trim());
        }else{
            throw new QuizException(QuizExceptionCode.NOT_FOUND_ERROR);
        }
    }

    /**
     * 회원 사용자의 점수를 업데이트하는 메서드
     * 정답/오답 여부와 퀴즈 난이도에 따라 점수를 부여
     * - 정답: 퀴즈 타입 점수 × 난이도 경험치
     * - 오답: 기본 점수 1점
     * 
     * @param user 사용자 정보 (null인 경우 비회원으로 점수 업데이트 안함)
     * @param quiz 퀴즈 정보
     * @param isAnswerCorrect 답변 정답 여부
     */
    private void updateUserScore(User user, Quiz quiz, boolean isAnswerCorrect) {
        if(user != null){
            double updatedScore;
            if(isAnswerCorrect){
                // 정답: 퀴즈 타입 점수 × 난이도 경험치 획득
                updatedScore = user.getScore() + (quiz.getType().getScore() * quiz.getLevel().getExp());
            }else{
                // 오답: 참여 점수 1점 획득
                updatedScore = user.getScore() + 1;
            }
            user.updateScore(updatedScore);
        }
    }

    /**
     * 이미 답변한 객체를 검증하는 메서드
     * @param userQuizAnswer 답변 객체
     */
    private void validateDuplicatedUserAnswer(UserQuizAnswer userQuizAnswer) {
        if(userQuizAnswer.getUser() == null){
            throw new UserException(UserExceptionCode.NOT_FOUND_USER);
        }
        if(userQuizAnswer.getQuiz() == null){
            throw new QuizException(QuizExceptionCode.NOT_FOUND_ERROR);
        }
        if(userQuizAnswer.getQuiz().getType() == null){
            throw new QuizException(QuizExceptionCode.QUIZ_CATEGORY_NOT_FOUND_ERROR);
        }
        if(userQuizAnswer.getSubscription() == null){
            throw new SubscriptionException(SubscriptionExceptionCode.NOT_FOUND_SUBSCRIPTION_ERROR);
        }
        // AI 피드백 생성중에 비정상적인 종료했을 경우
        if(userQuizAnswer.getAiFeedback() == null && userQuizAnswer.getIsCorrect() == null){
            throw new UserQuizAnswerException(UserQuizAnswerExceptionCode.AI_ANSWER_INVALID_ANSWER);
        }
        if(userQuizAnswer.getIsCorrect() == null){
            throw new UserQuizAnswerException(UserQuizAnswerExceptionCode.CORRECT_STATUS_INVALID_ANSWER);
        }
    }

    /**
     * 서술형에 대한 답변인지 확인하는 메서드
     * 퀴즈객체의 타입이 서술형이고, 답변객체의 AI 피드백이 null이 아니어야 한다.
     *
     * @param userQuizAnswer 답변 객체
     * @param quiz 퀴즈 객체
     * @return true/false 반환
     */
    private boolean getSubjectiveAnswerStatus(UserQuizAnswer userQuizAnswer, Quiz quiz) {
        return userQuizAnswer.getAiFeedback() != null &&
            quiz.getType().equals(QuizFormatType.SUBJECTIVE);
    }
}
