package com.example.cs25service.domain.admin.service;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25service.domain.quiz.dto.QuizCategoryRequestDto;
import com.example.cs25service.domain.quiz.dto.QuizCategoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizCategoryAdminService {

    private final QuizCategoryRepository quizCategoryRepository;

    @Transactional
    public void createQuizCategory(QuizCategoryRequestDto request) {

        quizCategoryRepository.findByCategoryType(request.getCategory())
            .ifPresent(c -> {
                throw new QuizException(QuizExceptionCode.QUIZ_CATEGORY_ALREADY_EXISTS_ERROR);
            });

        QuizCategory parent = null;
        if (request.getParentId() != null) {
            parent = quizCategoryRepository.findById(request.getParentId())
                .orElseThrow(() ->
                    new QuizException(QuizExceptionCode.PARENT_QUIZ_CATEGORY_NOT_FOUND_ERROR));
        }

        QuizCategory quizCategory = QuizCategory.builder()
            .categoryType(request.getCategory())
            .parent(parent)
            .build();

        quizCategoryRepository.save(quizCategory);
    }

    @Transactional
    public QuizCategoryResponseDto updateQuizCategory(Long quizCategoryId, QuizCategoryRequestDto request) {
        QuizCategory quizCategory = quizCategoryRepository.findByIdOrElseThrow(quizCategoryId);

        if(request.getCategory() != null){
            quizCategoryRepository.findByCategoryType(request.getCategory())
                .filter(existingCategory -> !existingCategory.getId().equals(quizCategoryId))
                .ifPresent(c -> {
                    throw new QuizException(QuizExceptionCode.QUIZ_CATEGORY_ALREADY_EXISTS_ERROR);
                });
        }

        quizCategory.updateCategoryType(request.getCategory());

        if(request.getParentId() != null){
            QuizCategory parentQuizCategory = quizCategoryRepository.findByIdOrElseThrow(request.getParentId());
            quizCategory.setParent(parentQuizCategory);
        }

        return QuizCategoryResponseDto.builder()
            .main(quizCategory.getParent() != null
                ? quizCategory.getParent().getCategoryType()
                : null)
            .sub(quizCategory.getCategoryType())
            .build();
    }

    @Transactional
    public void deleteQuizCategory(Long quizCategoryId){
        if (!quizCategoryRepository.existsById(quizCategoryId)) {
            throw new QuizException(QuizExceptionCode.QUIZ_CATEGORY_NOT_FOUND_ERROR);
        }
        quizCategoryRepository.deleteById(quizCategoryId);
    }
}
