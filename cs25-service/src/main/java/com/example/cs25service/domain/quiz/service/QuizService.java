package com.example.cs25service.domain.quiz.service;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.user.entity.Role;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.exception.UserExceptionCode;
import com.example.cs25service.domain.mail.dto.MailLogResponse;
import com.example.cs25service.domain.quiz.dto.CreateQuizDto;
import com.example.cs25service.domain.quiz.dto.QuizResponseDto;
import com.example.cs25entity.domain.quiz.dto.QuizSearchDto;
import com.example.cs25service.domain.security.dto.AuthUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final QuizRepository quizRepository;
    private final QuizCategoryRepository quizCategoryRepository;

    @Transactional
    public void uploadQuizJson(
        MultipartFile file,
        String categoryType,
        QuizFormatType formatType
    ) {

        try {
            //대분류 확인
            QuizCategory category = quizCategoryRepository.findByCategoryType(categoryType)
                .orElseThrow(
                    () -> new QuizException(QuizExceptionCode.QUIZ_CATEGORY_NOT_FOUND_ERROR));

            //소분류 조회하기
            List<QuizCategory> childCategory = category.getChildren();

            //file 내용을 읽어 Dto 로 만들기
            CreateQuizDto[] quizArray = objectMapper.readValue(file.getInputStream(),
                CreateQuizDto[].class);

            //유효성 검증
            for (CreateQuizDto dto : quizArray) {
                //유효성 검증에 실패한 데이터를 Set 에 저장
                Set<ConstraintViolation<CreateQuizDto>> violations = validator.validate(dto);
                if (!violations.isEmpty()) {
                    throw new ConstraintViolationException("유효성 검증 실패", violations);
                }
            }

            // 1. 소분류 카테고리 맵으로 변환
            Map<String, QuizCategory> categoryMap = childCategory.stream()
                .collect(Collectors.toMap(
                    QuizCategory::getCategoryType,
                    Function.identity()
                ));

            // 2. 퀴즈 DTO → 엔티티로 변환
            List<Quiz> quizzes = Arrays.stream(quizArray)
                .map(dto -> {
                    QuizCategory subCategory = categoryMap.get(dto.getCategory());
                    if (subCategory == null) {
                        throw new IllegalArgumentException(
                            "소분류 카테고리가 존재하지 않습니다: " + dto.getCategory());
                    }

                    return Quiz.builder()
                        .type(formatType)
                        .question(dto.getQuestion())
                        .choice(dto.getChoice())
                        .answer(dto.getAnswer())
                        .commentary(dto.getCommentary())
                        .category(subCategory)
                        .level(dto.getLevel())
                        .build();
                })
                .toList();

            quizRepository.saveAll(quizzes);
        } catch (IOException e) {
            throw new QuizException(QuizExceptionCode.JSON_PARSING_FAILED_ERROR);
        } catch (ConstraintViolationException e) {
            throw new QuizException(QuizExceptionCode.QUIZ_VALIDATION_FAILED_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public QuizResponseDto getQuiz(Long id) {
        Quiz quiz = quizRepository.findByIdOrElseThrow(id);

        return QuizResponseDto.builder()
            .id(quiz.getId())
            .question(quiz.getQuestion())
            .answer(quiz.getAnswer())
            .commentary(quiz.getCommentary() != null ? quiz.getCommentary() : null)
            .level(quiz.getLevel())
            .build();
    }

    @Transactional(readOnly = true)
    public Page<QuizResponseDto> getQuizzes(QuizSearchDto condition, Pageable pageable){

        return quizRepository.searchQuizzes(condition, pageable)
            .map(QuizResponseDto::from);

    }

    @Transactional
    public void deleteQuizzes(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("삭제할 퀴즈를 선택해주세요.");
        }

        quizRepository.deleteAllByIdIn(ids);
    }
}
