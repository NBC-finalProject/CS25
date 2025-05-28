package com.example.cs25.domain.users.vo;


import com.example.cs25.global.entity.BaseEntity;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@NoArgsConstructor
@RequiredArgsConstructor
@Embeddable
public class Subscription extends BaseEntity {

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private boolean isActive;

    private int subscriptionType; // "월화수목금토일" => "1111111" , "월수금" => "1010100"

    private Long categoryId;

    /**
     * Constructs a Subscription with the specified start and end dates, active status, subscription type, and category ID.
     *
     * @param startDate the start date and time of the subscription period
     * @param endDate the end date and time of the subscription period
     * @param isActive true if the subscription is currently active; false otherwise
     * @param subscriptionType an integer encoding the days of the week the subscription applies to
     * @param categoryId the identifier of the category associated with the subscription
     */
    @Builder
    public Subscription(LocalDateTime startDate, LocalDateTime endDate,
        boolean isActive,
        int subscriptionType, Long categoryId) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.subscriptionType = subscriptionType;
        this.categoryId = categoryId;
    }
}
