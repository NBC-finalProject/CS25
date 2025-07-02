package com.example.cs25service.domain.subscription.service;

import static com.example.cs25entity.domain.subscription.entity.Subscription.decodeDays;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.entity.SubscriptionHistory;
import com.example.cs25entity.domain.subscription.exception.SubscriptionException;
import com.example.cs25entity.domain.subscription.exception.SubscriptionExceptionCode;
import com.example.cs25entity.domain.subscription.repository.SubscriptionHistoryRepository;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25service.domain.subscription.dto.SubscriptionRequestDto;
import com.example.cs25service.domain.subscription.dto.SubscriptionResponseDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final QuizCategoryRepository quizCategoryRepository;
    private final UserRepository userRepository;

    /**
     * 구독아이디로 구독정보를 조회하는 메서드
     *
     * @param subscriptionId 구독 아이디
     * @return 구독정보 DTO 반환
     */
    @Transactional(readOnly = true)
    public SubscriptionInfoDto getSubscription(String subscriptionId) {
        Subscription subscription = subscriptionRepository.findBySerialIdOrElseThrow(subscriptionId);

        //구독 시작, 구독 종료 날짜 기반으로 구독 기간 계산
        LocalDate start = subscription.getStartDate();
        LocalDate end = subscription.getEndDate();
        long period = ChronoUnit.DAYS.between(start, end);

        return SubscriptionInfoDto.builder()
            .category(subscription.getCategory().getCategoryType())
            .email(subscription.getEmail())
            .days(decodeDays(subscription.getSubscriptionType()))
            .active(subscription.isActive())
            .startDate(subscription.getStartDate())
            .endDate(subscription.getEndDate())
            .period(period)
            .build();
    }

    /**
     * 구독정보를 생성하는 메서드
     *
     * @param requestDto  사용자를 통해 받은 생성할 구독 정보
     * @param authUser 로그인 정보
     * @return 구독 응답 DTO를 반환
     */
    @Transactional
    public SubscriptionResponseDto createSubscription(
        SubscriptionRequestDto requestDto, AuthUser authUser) {

        // 퀴즈 카테고리 불러오기
        QuizCategory quizCategory = quizCategoryRepository.findByCategoryTypeOrElseThrow(
            requestDto.getCategory());

        // 퀴즈 카테고리가 대분류인지 검증
        if (quizCategory.isChildCategory()) {
            throw new QuizException(QuizExceptionCode.PARENT_CATEGORY_REQUIRED_ERROR);
        }

        // 로그인을 한 경우
        if (authUser != null) {
            return createSubscriptionWithLogin(authUser, requestDto, quizCategory);
        // 비로그인일 경우
        } else {
            return createSubscriptionWithLogout(requestDto, quizCategory);
        }
    }

    /**
     * 구독정보를 업데이트하는 메서드
     *
     * @param subscriptionId 구독 아이디
     * @param requestDto     사용자로부터 받은 업데이트할 구독정보
     */
    @Transactional
    public void updateSubscription(String subscriptionId, SubscriptionRequestDto requestDto
    ) {
        Subscription subscription = subscriptionRepository.findBySerialIdOrElseThrow(subscriptionId);
        QuizCategory quizCategory = quizCategoryRepository.findByCategoryTypeOrElseThrow(
            requestDto.getCategory());

        LocalDate requestDate = subscription.getEndDate()
            .plusMonths(requestDto.getPeriod().getMonths());

        LocalDate maxSubscriptionDate = subscription.getStartDate().plusYears(1);

        if (requestDate.isAfter(maxSubscriptionDate)) {
            throw new SubscriptionException(
                SubscriptionExceptionCode.ILLEGAL_SUBSCRIPTION_PERIOD_ERROR);
        }

        subscription.update(
            quizCategory,
            requestDto.getDays(),
            requestDto.isActive(),
            requestDto.getPeriod()
        );

        createSubscriptionHistory(subscription);
    }

    /**
     * 구독을 취소하는 메서드
     *
     * @param subscriptionId 구독 아이디
     */
    @Transactional
    public void cancelSubscription(String subscriptionId) {
        Subscription subscription = subscriptionRepository.findBySerialIdOrElseThrow(subscriptionId);

        subscription.updateDisable();
        createSubscriptionHistory(subscription);
    }

    /**
     * 비로그인 유저 구독을 생성하는 메서드
     *
     * @param requestDto 유저로부터 받은 요청 DTO
     * @param quizCategory 문제 분야
     * @return 구독 응답 DTO를 반환
     * @throws SubscriptionException 이미 구독중인 이메일이면 예외처리
     */
    private SubscriptionResponseDto createSubscriptionWithLogout(
        SubscriptionRequestDto requestDto, QuizCategory quizCategory
    ) {
        // 이메일 중복체크
        this.checkEmail(requestDto.getEmail());
        try {
            Subscription subscription = createAndSaveSubscription(requestDto, quizCategory);
            createSubscriptionHistory(subscription);

            return toSubscriptionResponseDto(subscription);
        } catch (DataIntegrityViolationException e) {
            // 이중방어로 이메일 중복(UNIQUE 제약조건) 예외 발생시
            throw new SubscriptionException(
                SubscriptionExceptionCode.DUPLICATE_SUBSCRIPTION_EMAIL_ERROR);
        }
    }

    /**
     * 로그인 유저 구독을 생성하는 메서드
     *
     * @param authUser 로그인 유저 정보
     * @param request 유저로부터 받은 요청 DTO
     * @param quizCategory 문제 분야
     * @return 구독 응답 DTO를 반환
     * @throws SubscriptionException 구독정보가 있으면 예외처리
     */
    private SubscriptionResponseDto createSubscriptionWithLogin(
        AuthUser authUser, SubscriptionRequestDto request, QuizCategory quizCategory
    ) {
        User user = userRepository.findBySerialIdOrElseThrow(authUser.getSerialId());

        // 구독 정보가 없어야 함
        if (user.getSubscription() == null) {
            // 구독 및 히스토리 생성
            Subscription subscription = createAndSaveSubscription(request, quizCategory);
            createSubscriptionHistory(subscription);

            // 로그인 유저 구독정보 업데이트
            user.updateSubscription(subscription);

            return toSubscriptionResponseDto(subscription);
        } else {
            throw new SubscriptionException(
                SubscriptionExceptionCode.DUPLICATE_SUBSCRIPTION_EMAIL_ERROR);
        }
    }

    /**
     * 요청 DTO로 구독을 생성하고 반환하는 메서드
     *
     * @param requestDto 요청 DTO
     * @param quizCategory 구독한 문제 분야
     * @return 구독 객체를 반환
     */
    private Subscription createAndSaveSubscription(SubscriptionRequestDto requestDto, QuizCategory quizCategory) {
        LocalDate nowDate = LocalDate.now();

        return subscriptionRepository.save(
            Subscription.builder()
                .email(requestDto.getEmail())
                .category(quizCategory)
                .startDate(nowDate)
                .endDate(nowDate.plusMonths(requestDto.getPeriod().getMonths()))
                .subscriptionType(requestDto.getDays())
                .build()
        );
    }

    /**
     * 구독객체로 응답 DTO를 생성하고 반환하는 메서드
     *
     * @param subscription 구독 객체
     * @return 구독 응답 DTO를 반환
     */
    private SubscriptionResponseDto toSubscriptionResponseDto(Subscription subscription) {
        return SubscriptionResponseDto.builder()
            .id(subscription.getId())
            .category(subscription.getCategory().getCategoryType())
            .startDate(subscription.getStartDate())
            .endDate(subscription.getEndDate())
            .subscriptionType(subscription.getSubscriptionType())
            .build();
    }

    /**
     * 구독정보가 수정될 때 구독내역을 생성하는 메서드
     *
     * @param subscription 구독 객체
     */
    private void createSubscriptionHistory(Subscription subscription) {

        LocalDate updateDate = Optional.ofNullable(subscription.getUpdatedAt())
            .map(LocalDateTime::toLocalDate)
            .orElse(LocalDate.now()); // 또는 적절한 기본값

        subscriptionHistoryRepository.save(
            SubscriptionHistory.builder()
                .category(subscription.getCategory())
                .subscription(subscription)
                .subscriptionType(subscription.getSubscriptionType())
                .startDate(subscription.getStartDate())
                .updateDate(updateDate) // 구독정보 수정일
                .build()
        );
    }

    /**
     * 이미 구독하고 있는 이메일인지 확인하는 메서드
     *
     * @param email 이메일
     */
    public void checkEmail(String email) {
        if (subscriptionRepository.existsByEmail(email)) {
            throw new SubscriptionException(
                SubscriptionExceptionCode.DUPLICATE_SUBSCRIPTION_EMAIL_ERROR);
        }
    }
}
