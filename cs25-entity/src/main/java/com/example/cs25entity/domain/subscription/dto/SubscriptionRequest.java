package com.example.cs25entity.domain.subscription.dto;


import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import com.example.cs25entity.domain.subscription.entity.SubscriptionPeriod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubscriptionRequest {

    @NotNull(message = "기술 분야 선택은 필수입니다.")
    private String category;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 비어있을 수 없습니다.")
    private String email;

    @NotEmpty(message = "구독주기는 한 개 이상 선택해야 합니다.")
    private Set<DayOfWeek> days;

    private boolean isActive;

    // 수정하면서 기간을 늘릴수도, 안늘릴수도 있음, 기본값은 0
    @NotNull
    private SubscriptionPeriod period;

    @Builder
    public SubscriptionRequest(SubscriptionPeriod period, boolean isActive, Set<DayOfWeek> days,
        String email, String category) {
        this.period = period;
        this.isActive = isActive;
        this.days = days;
        this.email = email;
        this.category = category;
    }
}
