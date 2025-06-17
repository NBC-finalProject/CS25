package com.example.cs25entity.domain.subscription.entity;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구독 비활성화 직전까지의 기록 또는 구독 정보가 수정되었을 때 생성되는 테이블
 * <p/>
 * 구독 활성화 시에는 Subscription 엔티티에만 정보가 존재하며, 다음의 경우에 SubscriptionHistory가 생성됨
 * <p>
 * [예시 1] 1월 1일부터 3월까지 구독 진행 중에, 2월 5일에 구독을 비활성화하면, → 1월 1일부터 2월 5일까지의 구독 정보가 SubscriptionHistory에
 * 기록됨.
 * <p>
 * [예시 2] 6월 6일부터 7월 30일까지 구독 진행 중에, 6월 9일에 구독 주기(subscriptionType)가 변경되면, → 6월 6일부터 6월 9일까지의 기존 구독
 * 정보가 SubscriptionHistory에 기록됨.
 **/
@Getter
@Entity
@NoArgsConstructor
public class SubscriptionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "DATE")
    private LocalDate startDate;

    @Column(columnDefinition = "DATE")
    private LocalDate updateDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private QuizCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    private int subscriptionType; // "월화수목금토일" => "1111111" , "월수금" => "1010100"

    @Builder
    public SubscriptionHistory(QuizCategory category, Subscription subscription,
        LocalDate startDate, LocalDate updateDate, int subscriptionType) {
        this.category = category;
        this.subscription = subscription;
        this.startDate = startDate;
        this.updateDate = updateDate;
        this.subscriptionType = subscriptionType;
    }
}
