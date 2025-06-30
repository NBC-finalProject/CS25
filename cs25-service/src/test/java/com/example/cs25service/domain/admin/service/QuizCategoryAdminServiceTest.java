package com.example.cs25service.domain.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25service.domain.quiz.dto.QuizCategoryRequestDto;
import com.example.cs25service.domain.quiz.dto.QuizCategoryResponseDto;
import com.example.cs25service.domain.quiz.service.QuizCategoryService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class QuizCategoryAdminServiceTest {
    @InjectMocks
    private QuizCategoryAdminService quizCategoryService;

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
    void createQuizCategory_withoutParent_throwQuizException() {
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
    @DisplayName("대분류 카테고리 이름만 업데이트")
    void updateQuizCategory_changeCategoryType_only() {
        //given
        QuizCategory quizCategory = QuizCategory.builder()
            .categoryType("BBACKEND")
            .parent(null)
            .build();
        ReflectionTestUtils.setField(quizCategory, "id", 1L);

        when(quizCategoryRepository.findByIdOrElseThrow(1L)).thenReturn(quizCategory);

        QuizCategoryRequestDto requestDto = QuizCategoryRequestDto.builder()
            .category("BACKEND")
            .parentId(null)
            .build();

        //when
        QuizCategoryResponseDto response = quizCategoryService.updateQuizCategory(1L, requestDto);

        //then
        assertNull(response.getMain());
        assertEquals("BACKEND", response.getSub());
    }

    @Test
    @DisplayName("카테고리의 부모와 이름 변경")
    void updateQuizCategory_changeCategoryType_andParent() {
        //given
        QuizCategory parent = QuizCategory.builder()
            .categoryType("PARENT")
            .parent(null)
            .build();
        QuizCategory child = QuizCategory.builder()
            .categoryType("SUB")
            .parent(null)
            .build();

        ReflectionTestUtils.setField(parent, "id", 1L);
        ReflectionTestUtils.setField(child, "id", 2L);

        when(quizCategoryRepository.findByIdOrElseThrow(1L)).thenReturn(parent);
        when(quizCategoryRepository.findByIdOrElseThrow(2L)).thenReturn(child);

        QuizCategoryRequestDto requestDto = QuizCategoryRequestDto.builder()
            .category("CHILD")
            .parentId(1L)
            .build();

        QuizCategoryResponseDto response = quizCategoryService.updateQuizCategory(2L, requestDto);

        assertEquals("CHILD", response.getSub());
        assertEquals("PARENT", response.getMain());
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteQuizCategory_success() {
        // given
        when(quizCategoryRepository.existsById(1L)).thenReturn(true);

        // when
        quizCategoryService.deleteQuizCategory(1L);

        // then
        verify(quizCategoryRepository).existsById(1L);
        verify(quizCategoryRepository).deleteById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 삭제 시 QUIZ_CATEGORY_NOT_FOUND_ERROR 예외를 던짐")
    void deleteQuizCategory_notFound_shouldThrowException() {
        // given
        when(quizCategoryRepository.existsById(999L)).thenReturn(false);

        // when
        QuizException ex = assertThrows(QuizException.class,
            () -> quizCategoryService.deleteQuizCategory(999L));

        //then
        assertEquals(QuizExceptionCode.QUIZ_CATEGORY_NOT_FOUND_ERROR, ex.getErrorCode());
        verify(quizCategoryRepository, never()).deleteById(any());
    }
}