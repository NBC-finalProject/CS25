package com.example.cs25.domain.quiz.service;

import com.example.cs25.domain.quiz.dto.QuizResponseDto;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.exception.QuizException;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @InjectMocks
    private QuizService quizService;

    private Quiz quiz;
    private Long quizId = 1L;

    @BeforeEach
    void setup(){
         quiz = Quiz.builder()
                .question("1. 문제")
                .answer("1. 정답")
                .commentary("해설")
                .build();
    }
    @Test
    void getQuizDetail_문제_해설_정답_조회() {
        //given
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));

        //when
        QuizResponseDto quizDetail = quizService.getQuizDetail(quizId);

        //then
        assertThat(quizDetail.getQuestion()).isEqualTo(quiz.getQuestion());
        assertThat(quizDetail.getAnswer()).isEqualTo(quiz.getAnswer());
        assertThat(quizDetail.getCommentary()).isEqualTo(quiz.getCommentary());

    }

    @Test
    void getQuizDetail_문제가_없는_경우_예외(){
        //given
        when(quizRepository.findById(quizId)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() ->  quizService.getQuizDetail(quizId))
                .isInstanceOf(QuizException.class)
                .hasMessageContaining("해당 퀴즈를 찾을 수 없습니다");

    }

}