package com.example.cs25service.domain.profile.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class ProfileWrongQuizResponseDto {

    private final String userId;

    private final List<WrongQuizDto> wrongQuizList;

    public ProfileWrongQuizResponseDto(String userId, List<WrongQuizDto> wrongQuizList) {
        this.userId = userId;
        this.wrongQuizList = wrongQuizList;
    }
}
