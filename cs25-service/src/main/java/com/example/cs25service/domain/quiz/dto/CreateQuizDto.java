package com.example.cs25service.domain.quiz.dto;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateQuizDto {
    @NotBlank(message = "문제는 필수입니다.")
    private String question;

    private String choice; //객관식이 아니면 보기는 null

    @NotBlank(message = "답안은 필수입니다.")
    private String answer;

    private String commentary; //해석이 없으면 null

    @NotBlank(message = "카테고리 설정은 필수입니다.")
    private String category;

    @NotNull(message = "난이도 선택은 필수입니다.")
    private QuizLevel level;

    @Builder
    public CreateQuizDto(String question, String choice, String answer, String commentary,
        String category, QuizLevel level) {
        this.question = question;
        this.choice = choice;
        this.answer = answer;
        this.commentary = commentary;
        this.category = category;
        this.level = level;
    }
}