package com.example.cs25entity.domain.quiz.repository;

import com.example.cs25entity.domain.quiz.dto.QuizSearchDto;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuizCustomRepository {

    List<Quiz> findAvailableQuizzesUnderParentCategory(Long parentCategoryId,
        List<QuizLevel> difficulties,
        Set<Long> solvedQuizIds,
        List<QuizFormatType> targetTypes);

    Page<Quiz> searchQuizzes(QuizSearchDto condition, Pageable pageable);
}
