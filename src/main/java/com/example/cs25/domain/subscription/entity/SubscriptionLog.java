package com.example.cs25.domain.subscription.entity;

import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class SubscriptionLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private QuizCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    private int subscriptionType; // "월화수목금토일" => "1111111" , "월수금" => "1010100"

    @Builder
    public SubscriptionLog(QuizCategory category, Subscription subscription, int subscriptionType){
        this.category = category;
        this.subscription = subscription;
        this.subscriptionType = subscriptionType;
    }
}
