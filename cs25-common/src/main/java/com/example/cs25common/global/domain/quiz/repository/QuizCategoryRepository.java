package com.example.cs25common.global.domain.quiz.repository;

import com.example.cs25common.global.domain.quiz.entity.QuizCategory;
import com.example.cs25common.global.domain.quiz.exception.QuizException;
import com.example.cs25common.global.domain.quiz.exception.QuizExceptionCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizCategoryRepository extends JpaRepository<QuizCategory, Long> {

    Optional<QuizCategory> findByCategoryType(String categoryType);

    default QuizCategory findByCategoryTypeOrElseThrow(String categoryType) {
        return findByCategoryType(categoryType)
            .orElseThrow(() ->
                new QuizException(QuizExceptionCode.QUIZ_CATEGORY_NOT_FOUND_ERROR));
    }

    @Query("SELECT q.id FROM QuizCategory q")
    List<Long> selectAllCategoryId();
}
