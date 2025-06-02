package com.example.cs25.domain.quiz.service;

import com.example.cs25.domain.quiz.dto.CreateQuizDto;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.quiz.entity.QuizCategoryType;
import com.example.cs25.domain.quiz.entity.QuizFormatType;
import com.example.cs25.domain.quiz.exception.QuizException;
import com.example.cs25.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final QuizRepository quizRepository;
    private final QuizCategoryRepository quizCategoryRepository;

    @Transactional
    public void uploadQuizJson(MultipartFile file, QuizCategoryType categoryType,
        QuizFormatType formatType) {
        try {
            QuizCategory category = quizCategoryRepository.findByCategoryType(categoryType)
                .orElseThrow(
                    () -> new QuizException(QuizExceptionCode.QUIZ_CATEGORY_NOT_FOUND_EVENT));

            CreateQuizDto[] quizArray = objectMapper.readValue(file.getInputStream(),
                CreateQuizDto[].class);

            for (CreateQuizDto dto : quizArray) {
                //유효성 검증에 실패한 데이터를 Set에 저장
                Set<ConstraintViolation<CreateQuizDto>> violations = validator.validate(dto);
                if (!violations.isEmpty()) {
                    throw new ConstraintViolationException("유효성 검증 실패", violations);
                }
            }

            List<Quiz> quizzes = Arrays.stream(quizArray)
                .map(dto -> Quiz.builder()
                    .type(formatType)
                    .question(dto.question())
                    .choice(dto.choice())
                    .answer(dto.answer())
                    .commentary(dto.commentary())
                    .category(category)
                    .build())
                .toList();

            quizRepository.saveAll(quizzes);
        } catch (IOException e) {
            throw new QuizException(QuizExceptionCode.JSON_PARSING_FAILED);
        } catch (ConstraintViolationException e) {
            throw new QuizException(QuizExceptionCode.QUIZ_VALIDATION_FAILED);
        }
    }


    @Transactional
    public int getTodayQuiz(Long subscriptionId) {
        //해당 구독자의 문제 구독 카테고리 확인

        //해당 구독자의 최근 문제 풀이 기록확인

        //다음 문제 내주기
        return 0;
    }
}
