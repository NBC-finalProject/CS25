package com.example.cs25.domain.quiz.repository;

import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.quiz.entity.QuizCategoryType;
import com.example.cs25.domain.quiz.exception.QuizException;
import com.example.cs25.domain.quiz.exception.QuizExceptionCode;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizCategoryRepository extends JpaRepository<QuizCategory, Long> {

    Optional<QuizCategory> findByCategoryType(QuizCategoryType categoryType);

    default QuizCategory findByIdOrElseThrow(QuizCategoryType categoryType){
        return findByCategoryType(categoryType)
            .orElseThrow(() ->
                new QuizException(QuizExceptionCode.QUIZ_CATEGORY_NOT_FOUND_EVENT));
    }
}
