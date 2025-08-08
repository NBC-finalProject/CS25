package com.example.cs25entity.domain.quiz.repository;

import com.example.cs25entity.domain.quiz.entity.QQuiz;
import com.example.cs25entity.domain.quiz.entity.QQuizCategory;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuizCustomRepositoryImpl implements QuizCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Quiz findAvailableQuizzesUnderParentCategory(Long parentCategoryId,
        List<QuizLevel> difficulties,
        Set<Long> solvedQuizIds,
        QuizFormatType targetType,
        int offset) {

        /* < 사용되는 쿼리문 >
            SELECT q.*
            FROM quiz q
            JOIN quiz_category qc ON q.quiz_category_id = qc.id
            WHERE qc.parent_id = ?
              AND q.level IN (?, ?, ...)
              AND q.type = ?
              AND q.quiz_category_id IS NOT NULL
              AND q.id NOT IN (?, ?, ...)
            ORDER BY q.id ASC
            LIMIT 1 OFFSET ?
        * */

        QQuiz quiz = QQuiz.quiz;
        QQuizCategory category = QQuizCategory.quizCategory;

        BooleanBuilder builder = new BooleanBuilder()
            .and(quiz.category.parent.id.eq(parentCategoryId))
            .and(quiz.level.in(difficulties))
            .and(quiz.type.eq(targetType))
            .and(quiz.category.id.isNotNull());

        if (!solvedQuizIds.isEmpty()) {
            builder.and(quiz.id.notIn(solvedQuizIds));
        }

        return queryFactory
            .selectFrom(quiz)
            .join(quiz.category, category)
            .where(builder)
            .orderBy(quiz.id.asc())
            .offset(offset)
            .limit(1)
            .fetchOne();
    }
}
