package com.example.cs25service.domain.subscription.dto;

import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import com.example.cs25entity.domain.subscription.entity.SubscriptionPeriod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubscriptionRequestDto {

    @NotNull(message = "분야 선택은 필수입니다.")
    private String category;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotEmpty(message = "요일은 반드시 한 개 이상 선택해야 합니다.")
    private Set<DayOfWeek> days;

    private boolean active;

    // 수정하면서 기간을 늘릴수도, 안늘릴수도 있음, 기본값은 0
    @NotNull(message = "구독기간연장 값이 올바르지 않습니다.")
    private SubscriptionPeriod period;

    @Builder
    public SubscriptionRequestDto(SubscriptionPeriod period, boolean active, Set<DayOfWeek> days,
        String email, String category) {
        this.period = period;
        this.active = active;
        this.days = days;
        this.email = email;
        this.category = category;
    }
}
