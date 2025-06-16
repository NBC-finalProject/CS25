package com.example.cs25common.global.domain.quiz.repository;

import com.example.cs25common.global.domain.quiz.entity.Quiz;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findAllByCategoryId(Long categoryId);

}
