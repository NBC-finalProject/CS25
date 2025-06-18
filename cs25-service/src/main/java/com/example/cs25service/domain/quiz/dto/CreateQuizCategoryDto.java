package com.example.cs25service.domain.quiz.dto;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateQuizCategoryDto {
    @NotBlank
    private String category;
    private Long parentId; //대분류면 null
}
