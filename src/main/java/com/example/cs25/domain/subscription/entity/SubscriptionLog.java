package com.example.cs25.domain.subscription.entity;

import java.time.LocalDate;

import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.quiz.entity.QuizCategoryType;
import com.example.cs25.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "subscription_log")
public class SubscriptionLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "DATE")
    private LocalDate startDate;

    @Column(columnDefinition = "DATE")
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private QuizCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    private int subscriptionType; // "월화수목금토일" => "1111111" , "월수금" => "1010100"

    @Builder
    public SubscriptionLog(QuizCategoryType category, Subscription subscription,
        LocalDate startDate, LocalDate endDate, int subscriptionType){
        this.category = new QuizCategory(category);
        this.subscription = subscription;
        this.startDate = startDate;
        this.endDate = endDate;
        this.subscriptionType = subscriptionType;
    }
}
