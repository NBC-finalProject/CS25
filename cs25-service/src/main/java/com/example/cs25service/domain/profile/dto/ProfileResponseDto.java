package com.example.cs25service.domain.profile.dto;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25service.domain.quiz.dto.QuizResponseDto;
import lombok.Getter;

import java.util.List;

@Getter
public class ProfileResponseDto {
    private final Long userId;

    private final List<WrongQuizResponseDto> wrongQuizList;

    public ProfileResponseDto(Long userId, List<WrongQuizResponseDto> wrongQuizList) {
        this.userId = userId;
        this.wrongQuizList = wrongQuizList;
    }
}
