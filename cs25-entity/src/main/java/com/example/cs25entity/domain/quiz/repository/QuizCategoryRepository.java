package com.example.cs25entity.domain.quiz.repository;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import java.util.List;
import java.util.Optional;
import javax.swing.text.html.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizCategoryRepository extends JpaRepository<QuizCategory, Long> {

    Optional<QuizCategory> findByCategoryType(String categoryType);

    default QuizCategory findByCategoryTypeOrElseThrow(String categoryType) {
        return findByCategoryType(categoryType)
            .orElseThrow(() ->
                new QuizException(QuizExceptionCode.QUIZ_CATEGORY_NOT_FOUND_ERROR));
    }

    Optional<QuizCategory> findById(Long id);

    default QuizCategory findByIdOrElseThrow(Long id){
        return findById(id)
            .orElseThrow(() ->
                new QuizException(QuizExceptionCode.QUIZ_CATEGORY_NOT_FOUND_ERROR));
    }

    //대분류 QuizCategory만 조회
    List<QuizCategory> findByParentIdIsNull();

    //대분류의 Id만 조회
    @Query("SELECT q.id FROM QuizCategory q WHERE q.parent IS NULL")
    List<Long> findParentCategoryIds();

    @Query("SELECT q.id FROM QuizCategory q")
    List<Long> selectAllCategoryId();

    @Query("SELECT sc.categoryType FROM User u " +
        "JOIN u.subscription s " +
        "JOIN s.category sc " +
        "WHERE u.id = :userId")
    QuizCategory findQuizCategoryByUserId(@Param("userId") Long userId);

}
