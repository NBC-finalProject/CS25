package com.example.cs25service.domain.subscription.dto;

import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDate;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({"category", "email", "days", "active", "startDate", "endDate", "period"})
public class SubscriptionInfoDto {
    private final String category; // 구독 카테고리
    private final String email; // 구독 이메일
    private final Set<DayOfWeek> days; // 구독하고 있는 요일
    private final boolean active; // 구독 활성화 여부
    private final LocalDate startDate; // 구독 시작 일자
    private final LocalDate endDate; // 구독 종료 일자
    private final long period; // 구독 중인 기간
}
