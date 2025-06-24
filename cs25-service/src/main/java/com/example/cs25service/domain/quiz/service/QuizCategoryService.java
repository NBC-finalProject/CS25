package com.example.cs25service.domain.quiz.service;


import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25service.domain.quiz.dto.QuizCategoryRequestDto;
import com.example.cs25service.domain.quiz.dto.QuizCategoryResponseDto;
import com.example.cs25service.domain.security.dto.AuthUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizCategoryService {

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

    @Transactional(readOnly = true)
    public List<String> getParentQuizCategoryList() {
        return quizCategoryRepository.findByParentIdIsNull() //대분류만 찾아오도록 변경
            .stream().map(QuizCategory::getCategoryType
            ).toList();
    }

    @Transactional
    public QuizCategoryResponseDto updateQuizCategory(Long quizCategoryId, QuizCategoryRequestDto request) {
        QuizCategory quizCategory = quizCategoryRepository.findByIdOrElseThrow(quizCategoryId);
        quizCategory.setCategoryType(request.getCategory());

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
