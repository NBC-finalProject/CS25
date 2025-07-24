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
    public List<Quiz> findAvailableQuizzesUnderParentCategory(Long parentCategoryId,
        List<QuizLevel> difficulties,
        Set<Long> solvedQuizIds,
        List<QuizFormatType> targetTypes) {

        QQuiz quiz = QQuiz.quiz;
        QQuizCategory category = QQuizCategory.quizCategory;

        // 2. 퀴즈 조회
        BooleanBuilder builder = new BooleanBuilder()
            .and(quiz.category.parent.id.eq(parentCategoryId)) //내가 정한 카테고리에
            .and(quiz.level.in(difficulties)) //정해진 난이도 그룹안에있으면서
            .and(quiz.type.in(targetTypes)) //퀴즈 타입은 이거야
            .and(quiz.category.id.isNotNull());

        if (!solvedQuizIds.isEmpty()) {
            builder.and(quiz.id.notIn(solvedQuizIds)); //혹시라도 구독자가 문제를 푼 이력잉 ㅣㅆ으면 그것도 제외해야햄
        }
        return queryFactory
            .selectFrom(quiz)
            .join(quiz.category, category)
            .where(builder)
            .limit(20)
            .fetch();
    }
}
