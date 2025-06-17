package com.example.cs25entity.domain.quiz.repository;

import com.example.cs25entity.domain.quiz.entity.QuizAccuracy;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface QuizAccuracyRedisRepository extends CrudRepository<QuizAccuracy, String> {

    List<QuizAccuracy> findAllByCategoryId(Long categoryId);
}
