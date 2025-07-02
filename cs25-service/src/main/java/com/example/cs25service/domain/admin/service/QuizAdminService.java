package com.example.cs25service.domain.admin.service;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.admin.dto.request.QuizCreateRequestDto;
import com.example.cs25service.domain.admin.dto.request.QuizUpdateRequestDto;
import com.example.cs25service.domain.admin.dto.response.QuizDetailDto;
import com.example.cs25service.domain.admin.dto.request.CreateQuizDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class QuizAdminService {

    private final QuizRepository quizRepository;
    private final UserQuizAnswerRepository quizAnswerRepository;

    private final QuizCategoryRepository quizCategoryRepository;

    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Transactional
    public void uploadQuizJson(
        MultipartFile file,
        String categoryType,
        QuizFormatType formatType
    ) {
        try {
            //대분류 확인
            QuizCategory category = quizCategoryRepository.findByCategoryTypeOrElseThrow(categoryType);

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
                        .level(QuizLevel.valueOf(dto.getLevel()))
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
    public Page<QuizDetailDto> getAdminQuizDetails(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        //퀴즈 불러오깅
        Page<Quiz> quizzes = quizRepository.findAllOrderByCreatedAtDesc(pageable);

        return quizzes.map(quiz ->
            QuizDetailDto.builder()
                .quizId(quiz.getId())
                .question(quiz.getQuestion())
                .answer(quiz.getAnswer())
                .commentary(quiz.getCommentary())
                .choice(quiz.getChoice())
                .type(quiz.getType().name())
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .category(quiz.getCategory().getCategoryType())
                .solvedCnt(quizAnswerRepository.countByQuizId(quiz.getId()))
                .build()
        );
    }

    @Transactional(readOnly = true)
    public QuizDetailDto getAdminQuizDetail(Long quizId) {
        Quiz quiz = quizRepository.findByIdOrElseThrow(quizId);

        return QuizDetailDto.builder()
            .quizId(quiz.getId())
            .question(quiz.getQuestion())
            .answer(quiz.getAnswer())
            .commentary(quiz.getCommentary())
            .choice(quiz.getChoice())
            .type(quiz.getType().name())
            .createdAt(quiz.getCreatedAt())
            .updatedAt(quiz.getUpdatedAt())
            .solvedCnt(quizAnswerRepository.countByQuizId(quiz.getId()))
            .build();
    }

    @Transactional
    public Long createQuiz(QuizCreateRequestDto requestDto) {
        QuizCategory category = quizCategoryRepository.findByCategoryTypeOrElseThrow(
            requestDto.getCategory());

        Quiz newQuiz = Quiz.builder()
            .category(category)
            .answer(requestDto.getAnswer())
            .choice(requestDto.getChoice())
            .commentary(requestDto.getCommentary())
            .question(requestDto.getQuestion())
            .build();

        return quizRepository.save(newQuiz).getId();
    }

    @Transactional
    public QuizDetailDto updateQuiz(@Positive Long quizId, QuizUpdateRequestDto requestDto) {
        Quiz quiz = quizRepository.findByIdOrElseThrow(quizId);

        // 카테고리
        if (StringUtils.hasText(requestDto.getCategory())) {
            QuizCategory category = quizCategoryRepository.findByCategoryTypeOrElseThrow(
                requestDto.getCategory());
            quiz.updateCategory(category);
        }

        // 문제(question)
        if (StringUtils.hasText(requestDto.getQuestion())) {
            quiz.updateQuestion(requestDto.getQuestion());
        }

        // 정답(answer)
        if (StringUtils.hasText(requestDto.getAnswer())) {
            quiz.updateAnswer(requestDto.getAnswer());
        }

        // 해설(commentary)
        if (StringUtils.hasText(requestDto.getCommentary())) {
            quiz.updateCommentary(requestDto.getCommentary());
        }

        // 퀴즈 타입 변경 및 choice 처리
        if (requestDto.getQuizType() != null && !quiz.getType().equals(requestDto.getQuizType())) {
            QuizFormatType newType = requestDto.getQuizType();

            if (newType == QuizFormatType.MULTIPLE_CHOICE) {
                if (!StringUtils.hasText(requestDto.getChoice())) {
                    throw new QuizException(QuizExceptionCode.MULTIPLE_CHOICE_REQUIRE_ERROR);
                }
                quiz.updateChoice(requestDto.getChoice());
            }
            quiz.updateType(newType);
        } else {
            // 타입이 안 바뀌었더라도 choice 수정이 들어왔는지 체크
            if (StringUtils.hasText(requestDto.getChoice())) {
                quiz.updateChoice(requestDto.getChoice());
            }
        }

        return QuizDetailDto.builder()
            .quizId(quiz.getId())
            .question(quiz.getQuestion())
            .answer(quiz.getAnswer())
            .commentary(quiz.getCommentary())
            .choice(quiz.getChoice())
            .type(quiz.getType().name())
            .category(quiz.getCategory().getCategoryType()) // enum to string
            .createdAt(quiz.getCreatedAt())
            .updatedAt(quiz.getUpdatedAt())
            .solvedCnt(quizAnswerRepository.countByQuizId(quiz.getId())) // 필요 시 따로 조회
            .build();
    }

    @Transactional
    public void deleteQuiz(@Positive Long quizId) {
        Quiz quiz = quizRepository.findByIdOrElseThrow(quizId);

        quiz.disableQuiz();
    }
}
