package com.example.cs25.domain.quiz.repository;

import com.example.cs25.domain.quiz.entity.QuizCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizCategoryRepository extends JpaRepository<QuizCategory, Long> {

    Optional<QuizCategory> findByCategoryType(String categoryType);
}
