package com.example.cs25entity.domain.quiz.repository;

import com.example.cs25entity.domain.quiz.dto.QuizSearchDto;
import com.example.cs25entity.domain.quiz.entity.QQuiz;
import com.example.cs25entity.domain.quiz.entity.QQuizCategory;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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

        // 1. 소분류 ID들 가져오기
        List<Long> subCategoryIds = queryFactory
            .select(category.id)
            .from(category)
            .where(category.parent.id.eq(parentCategoryId))
            .fetch();

        if (subCategoryIds.isEmpty()) {
            return List.of();
        }

        // 2. 퀴즈 조회
        BooleanBuilder builder = new BooleanBuilder()
            .and(quiz.category.id.in(subCategoryIds)) //내가 정한 카테고리에
            .and(quiz.level.in(difficulties)) //정해진 난이도 그룹안에있으면서
            .and(quiz.type.in(targetTypes)); //퀴즈 타입은 이거야

        if (!solvedQuizIds.isEmpty()) {
            builder.and(quiz.id.notIn(solvedQuizIds)); //혹시라도 구독자가 문제를 푼 이력잉 ㅣㅆ으면 그것도 제외해야햄
        }

        return queryFactory
            .selectFrom(quiz)
            .where(builder)
            .limit(20)
            .fetch();
    }

    @Override
    public Page<Quiz> searchQuizzes(QuizSearchDto condition, Pageable pageable) {

        QQuiz quiz = QQuiz.quiz;

        BooleanBuilder builder = new BooleanBuilder();

        if (condition.getCategoryId() != null) {
            builder.and(quiz.category.id.eq(condition.getCategoryId()));
        }

        if (condition.getLevel() != null) {
            builder.and(quiz.level.eq(condition.getLevel()));
        }

        List<Quiz> content = queryFactory
            .selectFrom(quiz)
            .where(builder)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(quiz.id.asc())
            .fetch();

        long total = queryFactory
            .select(quiz.count())
            .from(quiz)
            .where(builder)
            .fetchOne();

        return new PageImpl<>(content, pageable, Optional.of(total).orElse(0L));
    }
}
