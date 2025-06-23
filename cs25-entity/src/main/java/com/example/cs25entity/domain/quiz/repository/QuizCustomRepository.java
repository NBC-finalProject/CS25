package com.example.cs25entity.domain.quiz.repository;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import java.util.List;
import java.util.Set;

public interface QuizCustomRepository {

    List<Quiz> findAvailableQuizzesUnderParentCategory(Long parentCategoryId,
        List<QuizLevel> difficulties,
        Set<Long> solvedQuizIds,
        Set<Long> recentQuizIds,
        List<QuizFormatType> targetTypes);

}
