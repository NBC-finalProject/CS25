package com.example.cs25service.domain.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileResponseDto {
    private final String name;
    private final double score;
    private final int rank;
    private final String subscriptionId;
}
