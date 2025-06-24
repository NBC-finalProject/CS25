package com.example.cs25service.domain.quiz.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25service.domain.quiz.dto.QuizCategoryRequestDto;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

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
    @DisplayName("대분류 퀴즈 카테고리 생성 성공")
    void createQuizCategory_withoutParent_success() {
        //given
        QuizCategoryRequestDto quizCategoryRequestDto = QuizCategoryRequestDto.builder()
            .category("BACKEND")
            .build();

        when(quizCategoryRepository.findByCategoryType("BACKEND")).thenReturn(Optional.empty());

        //when
        quizCategoryService.createQuizCategory(quizCategoryRequestDto);

        //then
        verify(quizCategoryRepository).save(any(QuizCategory.class));
    }

    @Test
    @DisplayName("대분류 카테고리가 있을 때, 소분류 퀴즈 카테고리 생성 성공")
    void createQuizCategory_withParent_success() {
        //given
        QuizCategory parentCategory = QuizCategory.builder()
            .categoryType("BACKEND")
            .build();

        QuizCategoryRequestDto quizCategoryRequestDto = QuizCategoryRequestDto.builder()
            .category("DATABASE")
            .parentId(1L)
            .build();

        when(quizCategoryRepository.findByCategoryType("DATABASE")).thenReturn(Optional.empty());
        when(quizCategoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));

        //when
        quizCategoryService.createQuizCategory(quizCategoryRequestDto);

        //then
        verify(quizCategoryRepository).save(any(QuizCategory.class));
    }

    @Test
    @DisplayName("이미 동일한 카테고리가 존재할 때, QUIZ_CATEGORY_ALREADY_EXISTS_ERROR 예외를 던짐")
    void createQuizCategory_alreadyExist_throwQuizException() {
        //given
        QuizCategoryRequestDto request = QuizCategoryRequestDto.builder()
            .category("BACKEND")
            .build();

        when(quizCategoryRepository.findByCategoryType("BACKEND"))
            .thenReturn(Optional.of(mock(QuizCategory.class)));

        //when
        QuizException ex = assertThrows(QuizException.class,
            () -> quizCategoryService.createQuizCategory(request));

        //then
        assertEquals(QuizExceptionCode.QUIZ_CATEGORY_ALREADY_EXISTS_ERROR, ex.getErrorCode());
    }

    @Test
    @DisplayName("소분류 카테고리 생성 시, 대분류(부모) 카테고리가 없으면 PARENT_QUIZ_CATEGORY_NOT_FOUND_ERROR 예외를 던짐")
    void createQuizCategory_withoutParent_throwQuizException(){
        //given
        QuizCategoryRequestDto request = QuizCategoryRequestDto.builder()
            .category("DATABASE")
            .parentId(1L)
            .build();

        when(quizCategoryRepository.findByCategoryType("DATABASE"))
            .thenReturn(Optional.empty());

        when(quizCategoryRepository.findById(request.getParentId()))
            .thenReturn(Optional.empty());

        //when
        QuizException ex = assertThrows(QuizException.class,
            () -> quizCategoryService.createQuizCategory(request));

        //then
        assertEquals(QuizExceptionCode.PARENT_QUIZ_CATEGORY_NOT_FOUND_ERROR, ex.getErrorCode());
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