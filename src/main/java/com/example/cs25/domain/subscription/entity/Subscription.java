package com.example.cs25.domain.subscription.entity;

import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.quiz.entity.QuizCategoryType;
import com.example.cs25.domain.subscription.dto.SubscriptionRequest;
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

    @Column(unique = true, nullable = false)
    private String email;

    @Column(columnDefinition = "DATE")
    private LocalDate startDate;

    @Column(columnDefinition = "DATE")
    private LocalDate endDate;

    private boolean isActive;

    private int subscriptionType; // "월화수목금토일" => "1111111"

    @Builder
    public Subscription(QuizCategory category, String email, LocalDate startDate,
        LocalDate endDate, Set<DayOfWeek> subscriptionType) {
        this.category = category;
        this.email = email;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = true;
        this.subscriptionType = encodeDays(subscriptionType);
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

    /**
     * 사용자가 입력한 값으로 구독정보를 업데이트하는 메서드
     * @param request 사용자를 통해 받은 구독 정보
     */
    public void update(SubscriptionRequest request) {
        this.category = new QuizCategory(request.getCategory());
        this.subscriptionType = encodeDays(request.getDays());
        this.isActive = request.isActive();
        this.endDate = endDate.plusMonths(request.getPeriod().getMonths());
    }

    /**
     * 구독취소하는 메서드
     */
    public void cancel(){
        this.isActive = false;
    }
}
