package com.example.cs25.domain.quiz.service;

import com.example.cs25.domain.quiz.dto.CreateQuizDto;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.quiz.entity.QuizCategoryType;
import com.example.cs25.domain.quiz.entity.QuizFormatType;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import com.example.cs25.domain.quiz.repository.QuizCategoryRepository;
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
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final QuizRepository quizRepository;
    private final QuizCategoryRepository quizcategoryRepository;

    public void uploadQuizJson(MultipartFile file, QuizCategoryType categoryType, QuizFormatType formatType){
        try {
            QuizCategory category = quizcategoryRepository.findByCategoryType(categoryType)
                .orElseThrow(); //예외 설정 필요

            CreateQuizDto[] quizArray = objectMapper.readValue(file.getInputStream(), CreateQuizDto[].class);

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
            throw new RuntimeException("JSON 파싱 실패", e);
        } catch (ConstraintViolationException e) {
            throw new RuntimeException(e);
        }
    }
}
