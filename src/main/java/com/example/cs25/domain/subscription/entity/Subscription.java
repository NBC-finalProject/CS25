package com.example.cs25.domain.subscription.entity;

import com.example.cs25.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "subscription")
public class Subscription extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private boolean isActive = false;

    private int subscriptionType; // "월화수목금토일" => "1111111" , "월수금" => "1010100"

    @Builder
    public Subscription (String email, LocalDateTime startDate, LocalDateTime endDate, boolean isActive, int subscriptionType){
        this.email = email;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.subscriptionType = subscriptionType;
    }
}
