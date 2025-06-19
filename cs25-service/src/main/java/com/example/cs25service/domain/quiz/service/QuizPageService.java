package com.example.cs25service.domain.quiz.service;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25service.domain.quiz.dto.TodayQuizResponseDto;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizPageService {

    private final QuizRepository quizRepository;

    public TodayQuizResponseDto setTodayQuizPage(Long quizId, Model model) {

        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new QuizException(QuizExceptionCode.NO_QUIZ_EXISTS_ERROR));

        List<String> choices = Arrays.stream(quiz.getChoice().split("/"))
            .filter(s -> !s.isBlank())
            .map(String::trim)
            .toList();
        String answerNumber = quiz.getAnswer().split("\\.")[0];

        return TodayQuizResponseDto.builder()
            .question(quiz.getQuestion())
            .choice1(choices.get(0))
            .choice2(choices.get(1))
            .choice3(choices.get(2))
            .choice4(choices.get(3))
            .answerNumber(answerNumber)
            .commentary(quiz.getCommentary())
            .build();
    }
}
