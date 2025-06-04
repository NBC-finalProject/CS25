package com.example.cs25.domain.quiz.repository;

import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.quiz.exception.QuizException;
import com.example.cs25.domain.quiz.exception.QuizExceptionCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizCategoryRepository extends JpaRepository<QuizCategory, Long> {

    Optional<QuizCategory> findByCategoryType(String categoryType);

    default QuizCategory findByCategoryTypeOrElseThrow(String categoryType) {
        return findByCategoryType(categoryType)
            .orElseThrow(() ->
                new QuizException(QuizExceptionCode.QUIZ_CATEGORY_NOT_FOUND_ERROR));
    }

}
