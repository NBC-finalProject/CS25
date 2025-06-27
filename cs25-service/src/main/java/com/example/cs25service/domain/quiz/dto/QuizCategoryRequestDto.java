package com.example.cs25service.domain.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizCategoryRequestDto {
    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;
    private Long parentId; //대분류면 null
}
