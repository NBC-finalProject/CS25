package com.example.cs25.domain.subscription.service;

import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25.domain.subscription.dto.SubscriptionRequest;
import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.entity.SubscriptionHistory;
import com.example.cs25.domain.subscription.exception.SubscriptionException;
import com.example.cs25.domain.subscription.exception.SubscriptionExceptionCode;
import com.example.cs25.domain.subscription.repository.SubscriptionHistoryRepository;
import com.example.cs25.domain.subscription.repository.SubscriptionRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

    /**
     * 구독아이디로 구독정보를 조회하는 메서드
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
            .subscriptionType(Subscription.decodeDays(subscription.getSubscriptionType()))
            .category(subscription.getCategory())
            .period(period)
            .build();
    }

    /**
     * 구독정보를 생성하는 메서드
     * @param request 사용자를 통해 받은 생성할 구독 정보
     */
    @Transactional
    public void createSubscription(SubscriptionRequest request) {
        if(subscriptionRepository.existsByEmail(request.getEmail())){
            throw new SubscriptionException(SubscriptionExceptionCode.DUPLICATE_SUBSCRIPTION_EMAIL_ERROR);
        }

        QuizCategory quizCategory = quizCategoryRepository.findByCategoryTypeOrElseThrow(request.getCategory());
        try {
            // FIXME: 이메일인증 완료되었다고 가정
            subscriptionRepository.save(
                Subscription.builder()
                    .email(request.getEmail())
                    .category(quizCategory)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(request.getPeriod().getDays()))
                    .subscriptionType(request.getDays())
                    .build()
            );
        } catch (DataIntegrityViolationException e) {
            // UNIQUE 제약조건 위반 시 발생하는 예외처리
            throw new SubscriptionException(SubscriptionExceptionCode.DUPLICATE_SUBSCRIPTION_EMAIL_ERROR);
        }
    }

    /**
     * 구독정보를 업데이트하는 메서드
     * @param subscriptionId 구독 아이디
     * @param request 사용자로부터 받은 업데이트할 구독정보
     */
    @Transactional
    public void updateSubscription(Long subscriptionId, SubscriptionRequest request) {
        Subscription subscription = subscriptionRepository.findByIdOrElseThrow(subscriptionId);

        subscription.update(request);
        createSubscriptionHistory(subscription);
    }

    /**
     * 구독을 취소하는 메서드
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
     * @param subscription 구독 객체
     */
    private void createSubscriptionHistory(Subscription subscription) {
        subscriptionHistoryRepository.save(
            SubscriptionHistory.builder()
                .category(subscription.getCategory())
                .subscription(subscription)
                .subscriptionType(subscription.getSubscriptionType())
                .startDate(subscription.getStartDate())
                .updateDate(subscription.getUpdatedAt().toLocalDate()) // 구독정보 수정일
                .build()
        );
    }
}
