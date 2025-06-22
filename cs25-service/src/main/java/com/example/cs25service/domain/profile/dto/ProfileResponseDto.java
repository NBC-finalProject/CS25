package com.example.cs25service.domain.profile.dto;

import lombok.Getter;

@Getter
public class ProfileResponseDto {
    private final String name;
    private final double score;
    private final int rank;

    public ProfileResponseDto(String name, double score, int rank) {
        this.name = name;
        this.score = score;
        this.rank = rank;
    }
}
