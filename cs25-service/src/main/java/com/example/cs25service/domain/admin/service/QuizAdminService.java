package com.example.cs25service.domain.admin.service;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.entity.QuizFormatType;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.admin.dto.request.QuizCreateRequestDto;
import com.example.cs25service.domain.admin.dto.request.QuizUpdateRequestDto;
import com.example.cs25service.domain.admin.dto.response.QuizDetailDto;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class QuizAdminService {

    private final QuizRepository quizRepository;
    private final UserQuizAnswerRepository quizAnswerRepository;

    private final QuizCategoryRepository quizCategoryRepository;

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
    //GET	관리자 문제  상세 조회	/admin/quizzes/{quizId}
    public QuizDetailDto getAdminQuizDetail(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() ->
                new QuizException(QuizExceptionCode.NOT_FOUND_ERROR));

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

    //POST	관리자 문제 등록	/admin/quizzes
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

    //PATCH	관리자 문제 수정	/admin/quizzes/{quizId}
    @Transactional
    public QuizDetailDto updateQuiz(@Positive Long quizId, QuizUpdateRequestDto requestDto) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new QuizException(QuizExceptionCode.NOT_FOUND_ERROR));

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
                if (!StringUtils.hasText(requestDto.getChoice()) || !StringUtils.hasText(
                    requestDto.getAnswer())) {
                    throw new QuizException(QuizExceptionCode.QUIZ_VALIDATION_FAILED_ERROR);
                }
                quiz.updateChoice(requestDto.getChoice());
            } else {
                quiz.updateChoice(null); // 주관식으로 변경되는 경우 choice 제거
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
            .solvedCnt(0L) // 필요 시 따로 조회
            .build();
    }

    //DELETE	관리자 문제 삭제	/admin/quizzes/{quizId}

}
