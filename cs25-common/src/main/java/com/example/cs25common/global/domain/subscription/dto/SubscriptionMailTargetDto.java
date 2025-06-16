package com.example.cs25common.global.domain.subscription.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SubscriptionMailTargetDto {

    private final Long subscriptionId;
    private final String email;
    private final String category;
}
