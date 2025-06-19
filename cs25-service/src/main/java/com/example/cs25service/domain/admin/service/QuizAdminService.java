package com.example.cs25service.domain.admin.service;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.admin.dto.QuizCreateRequestDto;
import com.example.cs25service.domain.admin.dto.QuizDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizAdminService {

    private final QuizRepository quizRepository;
    private final UserQuizAnswerRepository quizAnswerRepository;

    private final QuizCategoryRepository quizCategoryRepository;

    @Transactional(readOnly = true)
    public Page<QuizDetailDto> getQuizDetails(int page, int size) {
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
    public QuizDetailDto getQuizDetail(Long quizId) {
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
    //DELETE	관리자 문제 삭제	/admin/quizzes/{quizId}

}
