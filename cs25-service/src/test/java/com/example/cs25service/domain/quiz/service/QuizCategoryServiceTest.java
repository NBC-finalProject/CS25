package com.example.cs25service.domain.quiz.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class QuizCategoryServiceTest {

    @InjectMocks
    private QuizCategoryService quizCategoryService;

    @Mock
    private QuizCategoryRepository quizCategoryRepository;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("대분류 카테고리 조회 성공")
    void getParentQuizCategoryList_returnsCategoryTypes() {
        //given
        List<QuizCategory> parents = List.of(
            QuizCategory.builder().categoryType("BACKEND").build(),
            QuizCategory.builder().categoryType("FRONTEND").build()
        );
        when(quizCategoryRepository.findByParentIdIsNull()).thenReturn(parents);

        //when
        List<String> result = quizCategoryService.getParentQuizCategoryList();

        //then
        assertEquals(List.of("BACKEND", "FRONTEND"), result);
    }

    @Test
    @DisplayName("대분류 카테고리가 없으면 빈 List를 반환")
    void getParentQuizCategoryList_whenNone_returnsEmptyList() {
        when(quizCategoryRepository.findByParentIdIsNull()).thenReturn(Collections.emptyList());

        List<String> result = quizCategoryService.getParentQuizCategoryList();

        assertTrue(result.isEmpty());
    }

}