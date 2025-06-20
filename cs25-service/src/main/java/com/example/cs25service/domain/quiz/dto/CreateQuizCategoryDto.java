package com.example.cs25service.domain.quiz.dto;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class CreateQuizCategoryDto {
    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;
    private Long parentId; //대분류면 null
}
