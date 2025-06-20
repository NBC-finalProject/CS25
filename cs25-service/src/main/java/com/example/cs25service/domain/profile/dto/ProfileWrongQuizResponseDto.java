package com.example.cs25service.domain.profile.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ProfileWrongQuizResponseDto {
    private final Long userId;

    private final List<WrongQuizDto> wrongQuizList;

    public ProfileWrongQuizResponseDto(Long userId, List<WrongQuizDto> wrongQuizList) {
        this.userId = userId;
        this.wrongQuizList = wrongQuizList;
    }
}
