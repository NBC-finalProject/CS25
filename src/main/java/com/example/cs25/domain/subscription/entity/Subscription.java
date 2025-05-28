package com.example.cs25.domain.subscription.entity;

import com.example.cs25.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
public class Subscription extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private boolean isActive = false;

    private int subscriptionType; /**
     * Constructs a Subscription instance with the specified subscriber email, subscription period, active status, and subscription type.
     *
     * @param email the subscriber's email address
     * @param startDate the start date and time of the subscription
     * @param endDate the end date and time of the subscription
     * @param isActive true if the subscription is currently active; false otherwise
     * @param subscriptionType an integer encoding the days of the week for the subscription (e.g., 1111111 for all days, 1010100 for Monday, Wednesday, Friday)
     */

    @Builder
    public Subscription (String email, LocalDateTime startDate, LocalDateTime endDate, boolean isActive, int subscriptionType){
        this.email = email;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.subscriptionType = subscriptionType;
    }
}
