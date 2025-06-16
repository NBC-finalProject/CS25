package com.example.cs25service.domain.quiz.service;


import com.example.cs25common.global.domain.quiz.entity.QuizCategory;
import com.example.cs25common.global.domain.quiz.exception.QuizException;
import com.example.cs25common.global.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25common.global.domain.quiz.repository.QuizCategoryRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizCategoryService {

    private final QuizCategoryRepository quizCategoryRepository;

    @Transactional
    public void createQuizCategory(String categoryType) {
        Optional<QuizCategory> existCategory = quizCategoryRepository.findByCategoryType(
            categoryType);
        if (existCategory.isPresent()) {
            throw new QuizException(QuizExceptionCode.QUIZ_CATEGORY_ALREADY_EXISTS_ERROR);
        }

        QuizCategory quizCategory = new QuizCategory(categoryType);
        quizCategoryRepository.save(quizCategory);
    }

    @Transactional(readOnly = true)
    public List<String> getQuizCategoryList() {
        return quizCategoryRepository.findAll()
            .stream().map(QuizCategory::getCategoryType
            ).toList();
    }
}
