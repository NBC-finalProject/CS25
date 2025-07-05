package com.example.cs25service.domain.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RetryMailRequestDto {

    @NotEmpty(message = "재발송을 위한 구독 ID 목록은 비어있을 수 없습니다")
    List<Long> subscriptionIds;
}
