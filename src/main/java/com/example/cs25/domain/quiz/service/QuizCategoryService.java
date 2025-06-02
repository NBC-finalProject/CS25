package com.example.cs25.domain.quiz.service;

import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.quiz.entity.QuizCategoryType;
import com.example.cs25.domain.quiz.exception.QuizException;
import com.example.cs25.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25.domain.quiz.repository.QuizCategoryRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuizCategoryService {

    private final QuizCategoryRepository quizCategoryRepository;

    public void createQuizCategory(QuizCategoryType categoryType) {
        Optional<QuizCategory> existCategory = quizCategoryRepository.findByCategoryType(categoryType);
        if(existCategory.isPresent()){
            throw new QuizException(QuizExceptionCode.QUIZ_CATEGORY_ALREADY_EXISTS_EVENT);
        }

        QuizCategory quizCategory = new QuizCategory(categoryType);
        quizCategoryRepository.save(quizCategory);
    }
}
