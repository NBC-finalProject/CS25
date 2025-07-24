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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserQuizAnswerRepository userQuizAnswerRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final QuizCategoryRepository quizCategoryRepository;

    /**
     * 로그인 유저 구독 정보 조회하는 메서드
     * @param authUser 로그인 유저
     * @return 구독정보 DTO를 반환
     */
    public UserSubscriptionResponseDto getUserSubscription(AuthUser authUser) {

        // 사용자 정보 조회
        User user = userRepository.findBySerialIdOrElseThrow(authUser.getSerialId());

        // 해당 유저가 현재 사용 중인 구독 id 조회
        Long subscriptionId = user.getSubscription().getId();

        // 현재 구독의 상세 정보 조회
        SubscriptionInfoDto subscriptionInfo = subscriptionService.getSubscription(
            user.getSubscription().getSerialId());

        // 해당 구독의 이력(SubscriptionHistory) 전체 조회
        List<SubscriptionHistory> subLogs = subscriptionHistoryRepository
            .findAllBySubscriptionId(subscriptionId);
        // 조회한 구독 이력 엔티티 리스트를 DTO로 변환 (정적 팩토리 메소드 사용)
        List<SubscriptionHistoryDto> dtoList = subLogs.stream()
            .map(SubscriptionHistoryDto::fromEntity) // fromEntity(log -> SubscriptionHistoryDto.fromEntity(log))
            .toList();

        // 사용자 정보, 구독 상세, 구독 이력 로그를 포함한 응답 DTO 생성 및 반환
        return UserSubscriptionResponseDto.builder()
            .userId(user.getSerialId())
            .email(user.getEmail())
            .name(user.getName())
            .subscriptionLogPage(dtoList)
            .subscriptionInfoDto(subscriptionInfo)
            .build();
    }

    /**
     * 로그인 유저의 틀린문제 조회 메서드
     * @param authUser 로그인 유저 정보
     * @param pageable 페이징 객체
     * @return 틀린문제 DTO를 반환
     */
    public ProfileWrongQuizResponseDto getWrongQuiz(AuthUser authUser, Pageable pageable) {

        // 현재 로그인한 사용자 정보 조회
        User user = userRepository.findBySerialIdOrElseThrow(authUser.getSerialId());

        // 사용자가 틀린 퀴즈만 조회 (isCorrect = false), 페이징 적용
        Page<UserQuizAnswer> page = userQuizAnswerRepository.findAllByUserIdAndIsCorrectFalse(user.getId(), pageable);

        // 틀린 문제 리스트를 WrongQuizDto로 변환 (문제, 사용자의 답, 정답, 해설 포함)
        List<WrongQuizDto> wrongQuizList = page.stream()
            .map(answer -> new WrongQuizDto(
                answer.getQuiz().getQuestion(),     // 문제
                answer.getUserAnswer(),             // 사용자가 입력한 답안
                answer.getQuiz().getAnswer(),       // 정답
                answer.getQuiz().getCommentary()    // 해설
            ))
            .collect(Collectors.toList());

        // 사용자 id, 틀린 문제 리스트, 페이지 정보를 포함한 응답 DTO 생성 및 변환
        return new ProfileWrongQuizResponseDto(authUser.getSerialId(), wrongQuizList, page);
    }

    /**
     * 프로필 정보를 조회
     * @param authUser 로그인 정보
     * @return 프로필 정보를 반환 (이름, 랭킹, 점수, 구독 id)
     */
    public ProfileResponseDto getProfile(AuthUser authUser) {

        // 사용자 정보 조회
        User user = userRepository.findBySerialIdOrElseThrow(authUser.getSerialId());
        // 내 랭킹 조회 (조회 쿼리: 내 점수보다 큰 사용자 조회 해서 카운팅 하고 + 1)
        int myRank = userRepository.findRankByScore(user.getScore());

        // 유저가 구독을 했는지 안했는지 검증
        boolean userSubscriptionStatus = getUserSubscriptionStatus(user);

        return ProfileResponseDto.builder()
            .name(user.getName())
            .rank(myRank)
            .score(user.getScore())
            .subscriptionId(userSubscriptionStatus ? user.getSubscription().getSerialId() : null)
            .build();
    }

    //유저의 소분류 카테고리별 정답률 조회
    public CategoryUserAnswerRateResponse getUserQuizAnswerCorrectRate(AuthUser authUser) {

        //유저 검증
        User user = userRepository.findBySerialIdOrElseThrow(authUser.getSerialId());

        // 사용자에게 구독정보가 없으면 예외처리
        if(!getUserSubscriptionStatus(user)) {
            throw new UserException(UserExceptionCode.NOT_FOUND_SUBSCRIPTION);
        }

        // 사용자에게 구독정보가 없으면 예외처리
        if(user.getSubscription() == null) {
            throw new UserException(UserExceptionCode.NOT_FOUND_SUBSCRIPTION);
        }

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

    /**
     * 유저의 구독정보가 있는지 확인하는 메서드
     * @param user 유저 정보
     * @return 있으면 true, 없으면 false를 반환
     */
    private boolean getUserSubscriptionStatus(User user) {
        return user.getSubscription() != null;
    }
}
