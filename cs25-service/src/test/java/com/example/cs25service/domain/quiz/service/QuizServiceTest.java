package com.example.cs25service.domain.quiz.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25service.domain.quiz.dto.CreateQuizDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @InjectMocks
    private QuizService quizService;

    @Mock
    private QuizRepository quizRepository;
    @Mock
    private QuizCategoryRepository quizCategoryRepository;

    @Test
    @DisplayName("퀴즈 생성 성공")
    void createQuiz_success() {
        //given
        CreateQuizDto dto = CreateQuizDto
            .builder()
            .type("SHORT_ANSWER")
            .category("BACKEND")
            .question("오늘 내 점심 메뉴는?")
            .answer("안 궁금해")
            .level("EASY")
            .build();

        when(quizCategoryRepository.findByCategoryTypeOrElseThrow("BACKEND"))
            .thenReturn(mock(QuizCategory.class));

        //when
        quizService.createQuiz(dto);

        //then
        verify(quizRepository).save(any());
    }

    @Test
    @DisplayName("퀴즈 생성 시, 카테고리 없으면 QUIZ_CATEGORY_NOT_FOUND_ERROR 예외를 던짐")
    void createQuiz_withoutCategory_throwQuizException() {
        //given
        CreateQuizDto dto = CreateQuizDto
            .builder()
            .type("SHORT_ANSWER")
            .category("BACKEND")
            .question("오늘 내 점심 메뉴는?")
            .answer("안 궁금해")
            .level("EASY")
            .build();

        when(quizCategoryRepository.findByCategoryTypeOrElseThrow("BACKEND"))
            .thenThrow(new QuizException(QuizExceptionCode.QUIZ_CATEGORY_NOT_FOUND_ERROR));

        //when
        QuizException ex = assertThrows(QuizException.class, () -> quizService.createQuiz(dto));
        assertEquals(QuizExceptionCode.QUIZ_CATEGORY_NOT_FOUND_ERROR, ex.getErrorCode());

        //then
        verify(quizRepository, never()).save(any());
    }

    @Test
    @DisplayName("퀴즈 삭제 성공")
    void deleteQuizzes_success() {
        quizService.deleteQuizzes(List.of(1L, 2L));
        verify(quizRepository).deleteAllByIdIn(List.of(1L, 2L));
    }

    @Test
    @DisplayName("List가 비어있으면 퀴즈 삭제 IllegalArgumentException 예외를 던짐")
    void deleteQuizzes_withEmptyList_throwIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> quizService.deleteQuizzes(List.of()));

        assertEquals("삭제할 퀴즈를 선택해주세요.", ex.getMessage());
    }
}