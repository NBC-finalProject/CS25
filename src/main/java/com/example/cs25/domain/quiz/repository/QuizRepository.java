package com.example.cs25.domain.quiz.repository;

import com.example.cs25.domain.quiz.entity.Quiz;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findAllByCategoryId(Long categoryId);

}
