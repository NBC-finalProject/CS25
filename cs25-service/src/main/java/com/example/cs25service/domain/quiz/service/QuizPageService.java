package com.example.cs25service.domain.quiz.service;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizFormatType;
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

		return switch (quiz.getType()) {
			case MULTIPLE_CHOICE -> getMultipleQuiz(quiz);
			case SUBJECTIVE -> getSubjectiveQuiz(quiz);
			default -> throw new QuizException(QuizExceptionCode.QUIZ_TYPE_NOT_FOUND_ERROR);
		};
	}

    /**
     * 객관식인 오늘의 문제를 만들어서 반환해주는 메서드
     * @param quiz 문제 객체
     * @return 객관식 문제를 DTO로 반환
     */
    private TodayQuizResponseDto getMultipleQuiz(Quiz quiz) {
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
            .quizType(quiz.getType().name())
            .build();
    }

    /**
     * 주관식인 오늘의 문제를 만들어서 반환해주는 메서드
     * @param quiz 문제 객체
     * @return 주관식 문제를 DTO로 반환
     */
    private TodayQuizResponseDto getSubjectiveQuiz(Quiz quiz) {
        return TodayQuizResponseDto.builder()
			.question(quiz.getQuestion())
            .quizType(quiz.getQuestion())
            .answer(quiz.getAnswer())
            .commentary(quiz.getCommentary())
            .quizType(quiz.getType().name())
            .build();
    }
}
