package com.example.cs25service.domain.quiz.service;


import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25service.domain.quiz.dto.CreateQuizCategoryDto;
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
    public void createQuizCategory(CreateQuizCategoryDto request) {
        quizCategoryRepository.findByCategoryType(request.getCategory())
            .ifPresent(c -> {
                throw new QuizException(QuizExceptionCode.QUIZ_CATEGORY_ALREADY_EXISTS_ERROR);
            });

        QuizCategory parent = null;
        if (request.getParentId() != null) {
            parent = quizCategoryRepository.findById(request.getParentId())
                .orElseThrow(() ->
                    new QuizException(QuizExceptionCode.PARENT_QUIZ_CATEGORY_NOT_FOUND_ERROR));
        };

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
}
