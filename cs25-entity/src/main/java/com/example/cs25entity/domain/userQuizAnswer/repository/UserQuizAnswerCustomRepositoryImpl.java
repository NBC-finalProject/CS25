package com.example.cs25entity.domain.userQuizAnswer.repository;

import com.example.cs25entity.domain.quiz.entity.QQuiz;
import com.example.cs25entity.domain.quiz.entity.QQuizCategory;
import com.example.cs25entity.domain.subscription.entity.QSubscription;
import com.example.cs25entity.domain.userQuizAnswer.dto.UserAnswerDto;
import com.example.cs25entity.domain.userQuizAnswer.entity.QUserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserQuizAnswerCustomRepositoryImpl implements UserQuizAnswerCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<UserAnswerDto> findUserAnswerByQuizId(Long quizId) {
        QUserQuizAnswer userQuizAnswer = QUserQuizAnswer.userQuizAnswer;

        return queryFactory
            .select(Projections.constructor(UserAnswerDto.class, userQuizAnswer.userAnswer))
            .from(userQuizAnswer)
            .where(userQuizAnswer.quiz.id.eq(quizId))
            .fetch();
    }

    @Override
    public List<UserQuizAnswer> findByUserIdAndQuizCategoryId(Long userId, Long quizCategoryId) {
        QUserQuizAnswer answer = QUserQuizAnswer.userQuizAnswer;
        QQuiz quiz = QQuiz.quiz;
        QQuizCategory category = QQuizCategory.quizCategory;

        return queryFactory
            .selectFrom(answer)
            .join(answer.quiz, quiz)
            .join(quiz.category, category)
            .where(
                answer.user.id.eq(userId),
                category.parent.id.eq(quizCategoryId)
            )
            .fetch();
    }

    @Override
    public Set<Long> findRecentSolvedCategoryIds(Long userId, Long parentCategoryId,
        LocalDate afterDate) {
        QUserQuizAnswer answer = QUserQuizAnswer.userQuizAnswer;
        QQuiz quiz = QQuiz.quiz;
        QQuizCategory category = QQuizCategory.quizCategory;

        return new HashSet<>(queryFactory
            .select(category.id)
            .from(answer)
            .join(answer.quiz, quiz)
            .join(quiz.category, category)
            .where(
                answer.user.id.eq(userId),
                category.parent.id.eq(parentCategoryId),
                answer.createdAt.goe(afterDate.atStartOfDay())
            )
            .fetch());
    }

    @Override
    public Optional<UserQuizAnswer> findUserQuizAnswerBySerialIds(String quizSerialId, String subSerialId) {
        QUserQuizAnswer userQuizAnswer = QUserQuizAnswer.userQuizAnswer;
        QQuiz quiz = QQuiz.quiz;
        QSubscription subscription = QSubscription.subscription;

        return Optional.ofNullable(queryFactory.selectFrom(userQuizAnswer)
            .join(userQuizAnswer.quiz, quiz)
            .join(userQuizAnswer.subscription, subscription)
            .where(
                quiz.serialId.eq(quizSerialId),
                subscription.serialId.eq(subSerialId)
            )
            .fetchOne());
    }
}