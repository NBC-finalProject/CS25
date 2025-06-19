package com.example.cs25service.domain.subscription.service;

import static com.example.cs25entity.domain.subscription.entity.Subscription.*;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.entity.SubscriptionHistory;
import com.example.cs25entity.domain.subscription.exception.SubscriptionException;
import com.example.cs25entity.domain.subscription.exception.SubscriptionExceptionCode;
import com.example.cs25entity.domain.subscription.repository.SubscriptionHistoryRepository;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.exception.UserExceptionCode;
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
    public SubscriptionInfoDto getSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findByIdOrElseThrow(subscriptionId);

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
     * @param request 사용자를 통해 받은 생성할 구독 정보
     */
    @Transactional
    public SubscriptionResponseDto createSubscription(
        SubscriptionRequestDto request,
        AuthUser authUser) {

        // 퀴즈 카테고리 불러오기
        QuizCategory quizCategory = quizCategoryRepository.findByCategoryTypeOrElseThrow(
            request.getCategory());

        // 로그인 한 경우
        if (authUser != null) {
            User user = userRepository.findByEmail(authUser.getEmail()).orElseThrow(
                () -> new UserException(UserExceptionCode.NOT_FOUND_USER)
            );

            // TODO: 로그인을 해도 이메일 체크를 해야할까?
            // this.checkEmail(user.getEmail());

            // 구독 정보가 없는 경우
            if (user.getSubscription() == null) {
                LocalDate nowDate = LocalDate.now();
                Subscription subscription = subscriptionRepository.save(
                    Subscription.builder()
                        .email(user.getEmail())
                        .category(quizCategory)
                        .startDate(nowDate)
                        .endDate(nowDate.plusMonths(request.getPeriod().getMonths()))
                        .subscriptionType(request.getDays())
                        .build()
                );
                createSubscriptionHistory(subscription);
                return new SubscriptionResponseDto(
                    subscription.getId(),
                    subscription.getCategory(),
                    subscription.getStartDate(),
                    subscription.getEndDate(),
                    subscription.getSubscriptionType()
                );
            } else {
                // TODO: 로그인 했을때 구독정보가 있는데 다시 구독하기 눌렀을때 예외 처리
                throw new SubscriptionException(
                    SubscriptionExceptionCode.DUPLICATE_SUBSCRIPTION_EMAIL_ERROR);
            }
            // 비로그인 회원일 경우
        } else {
            // 이메일 체크
            this.checkEmail(request.getEmail());

            try {
                // FIXME: 이메일인증 완료되었다고 가정
                LocalDate nowDate = LocalDate.now();
                Subscription subscription = subscriptionRepository.save(
                    Subscription.builder()
                        .email(request.getEmail())
                        .category(quizCategory)
                        .startDate(nowDate)
                        .endDate(nowDate.plusMonths(request.getPeriod().getMonths()))
                        .subscriptionType(request.getDays())
                        .build()
                );
                createSubscriptionHistory(subscription);
                return new SubscriptionResponseDto(
                    subscription.getId(),
                    subscription.getCategory(),
                    subscription.getStartDate(),
                    subscription.getEndDate(),
                    subscription.getSubscriptionType()
                );
            } catch (DataIntegrityViolationException e) {
                // UNIQUE 제약조건 위반 시 발생하는 예외처리
                throw new SubscriptionException(
                    SubscriptionExceptionCode.DUPLICATE_SUBSCRIPTION_EMAIL_ERROR);
            }
        }
    }

    /**
     * 구독정보를 업데이트하는 메서드
     *
     * @param subscriptionId 구독 아이디
     * @param requestDto 사용자로부터 받은 업데이트할 구독정보
     */
    @Transactional
    public void updateSubscription(Long subscriptionId,
        SubscriptionRequestDto requestDto) {
        Subscription subscription = subscriptionRepository.findByIdOrElseThrow(subscriptionId);
        QuizCategory quizCategory = quizCategoryRepository.findByCategoryTypeOrElseThrow(
            requestDto.getCategory());

        LocalDate requestDate = subscription.getEndDate().plusMonths(requestDto.getPeriod().getMonths());
        LocalDate maxSubscriptionDate = subscription.getStartDate().plusYears(1);
        if(requestDate.isAfter(maxSubscriptionDate)){
            throw new SubscriptionException(SubscriptionExceptionCode.ILLEGAL_SUBSCRIPTION_PERIOD_ERROR);
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
    public void cancelSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findByIdOrElseThrow(subscriptionId);

        subscription.cancel();
        createSubscriptionHistory(subscription);
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
