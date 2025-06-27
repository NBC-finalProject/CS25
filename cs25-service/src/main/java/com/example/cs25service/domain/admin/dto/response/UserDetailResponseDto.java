package com.example.cs25service.domain.admin.dto.response;

import com.example.cs25service.domain.subscription.dto.SubscriptionHistoryDto;
import com.example.cs25service.domain.subscription.dto.SubscriptionInfoDto;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class UserDetailResponseDto {

    private final UserPageResponseDto userInfo;

    private final List<SubscriptionHistoryDto> subscriptionLog;

    private final SubscriptionInfoDto subscriptionInfo;
}
