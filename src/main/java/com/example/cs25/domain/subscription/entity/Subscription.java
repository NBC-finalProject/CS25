package com.example.cs25.domain.subscription.entity;

import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "subscription")
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quizCategory_id")
    private QuizCategory category;

    private String email;

    @Column(columnDefinition = "DATE")
    private LocalDate startDate;

    @Column(columnDefinition = "DATE")
    private LocalDate endDate;

    private boolean isActive = false;

    private int subscriptionType; // "월화수목금토일" => "1111111"

    @Builder
    public Subscription(QuizCategory category, String email, LocalDate startDate,
        LocalDate endDate,
        boolean isActive, int subscriptionType) {
        this.category = category;
        this.email = email;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.subscriptionType = subscriptionType;
    }

    // Set<DayOfWeek> → int
    public static int encodeDays(Set<DayOfWeek> days) {
        int result = 0;
        for (DayOfWeek day : days) {
            result |= day.getBitValue();
        }
        return result;
    }

    // int → Set<DayOfWeek>
    public static Set<DayOfWeek> decodeDays(int bits) {
        Set<DayOfWeek> result = EnumSet.noneOf(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            if (DayOfWeek.contains(bits, day)) {
                result.add(day);
            }
        }
        return result;
    }

    public void updateDisableSubscription() {
        this.isActive = false;
    }

    public void updateEnableSubscription() {
        this.isActive = true;
    }
}
