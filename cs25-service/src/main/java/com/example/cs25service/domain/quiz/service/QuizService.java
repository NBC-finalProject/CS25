package com.example.cs25service.domain.quiz.service;

import com.example.cs25common.global.domain.mail.service.MailService;
import com.example.cs25common.global.domain.quiz.dto.QuizResponseDto;
import com.example.cs25common.global.domain.quiz.entity.Quiz;
import com.example.cs25common.global.domain.quiz.entity.QuizCategory;
import com.example.cs25common.global.domain.quiz.entity.QuizFormatType;
import com.example.cs25common.global.domain.quiz.exception.QuizException;
import com.example.cs25common.global.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25common.global.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25common.global.domain.quiz.repository.QuizRepository;
import com.example.cs25common.global.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25service.domain.quiz.dto.CreateQuizDto;
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
    private final SubscriptionRepository subscriptionRepository;
    private final MailService mailService;

    @Transactional
    public void uploadQuizJson(MultipartFile file, String categoryType,
        QuizFormatType formatType) {
        try {
            QuizCategory category = quizCategoryRepository.findByCategoryType(categoryType)
                .orElseThrow(
                    () -> new QuizException(QuizExceptionCode.QUIZ_CATEGORY_NOT_FOUND_ERROR));

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
            throw new QuizException(QuizExceptionCode.JSON_PARSING_FAILED_ERROR);
        } catch (ConstraintViolationException e) {
            throw new QuizException(QuizExceptionCode.QUIZ_VALIDATION_FAILED_ERROR);
        }
    }

    public QuizResponseDto getQuizDetail(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new QuizException(QuizExceptionCode.NOT_FOUND_ERROR));
        return new QuizResponseDto(quiz.getQuestion(), quiz.getAnswer(), quiz.getCommentary());
    }
}
