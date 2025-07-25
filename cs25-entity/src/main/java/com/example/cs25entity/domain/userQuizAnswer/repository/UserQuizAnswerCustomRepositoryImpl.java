package com.example.cs25entity.domain.userQuizAnswer.repository;

import com.example.cs25entity.domain.quiz.entity.QQuiz;
import com.example.cs25entity.domain.quiz.entity.QQuizCategory;
import com.example.cs25entity.domain.subscription.entity.QSubscription;
import com.example.cs25entity.domain.user.entity.QUser;
import com.example.cs25entity.domain.userQuizAnswer.dto.UserAnswerDto;
import com.example.cs25entity.domain.userQuizAnswer.entity.QUserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.exception.UserQuizAnswerException;
import com.example.cs25entity.domain.userQuizAnswer.exception.UserQuizAnswerExceptionCode;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
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
                category.id.eq(quizCategoryId),
                answer.isCorrect.isNotNull()
            )
            .fetch();
    }

    @Override
    public Double getCorrectRate(Long subscriptionId, Long quizCategoryId) {
        /*  < 들어가는 쿼리 >
        * SELECT SUM(CASE WHEN uqa.is_correct = true THEN 1 ELSE 0 END) / COUNT(*)
            FROM user_quiz_answer uqa
            JOIN quiz q ON uqa.quiz_id = q.id
            JOIN quiz_category c ON q.quiz_category_id = c.id
            WHERE
                uqa.subscription_id = :subscriptionId
                AND c.parent_id = :quizCategoryId
        * */

        QUserQuizAnswer answer = QUserQuizAnswer.userQuizAnswer;
        QQuiz quiz = QQuiz.quiz;
        QQuizCategory category = QQuizCategory.quizCategory;

        // 정답 수
        NumberExpression<Integer> correctSum = new CaseBuilder()
            .when(answer.isCorrect.isTrue()).then(1)
            .otherwise(0)
            .sum();

        // 전체 수
        NumberExpression<Long> totalCount = answer.id.count();

        // 정답률 계산식
        NumberExpression<Double> correctRate = correctSum.doubleValue()
            .divide(totalCount.doubleValue());

        return queryFactory
            .select(correctRate)
            .from(answer)
            .join(answer.quiz, quiz)
            .join(quiz.category, category)
            .where(
                answer.subscription.id.eq(subscriptionId),
                category.parent.id.eq(quizCategoryId)
            )
            .fetchOne();
    }

    @Override
    public UserQuizAnswer findUserQuizAnswerBySerialIds(String quizSerialId, String subSerialId) {
        QUserQuizAnswer userQuizAnswer = QUserQuizAnswer.userQuizAnswer;
        QQuiz quiz = QQuiz.quiz;
        QSubscription subscription = QSubscription.subscription;
        QUser user = QUser.user;

        UserQuizAnswer result = queryFactory
            .selectFrom(userQuizAnswer)
            .leftJoin(userQuizAnswer.quiz, quiz).fetchJoin()
            .leftJoin(userQuizAnswer.subscription, subscription).fetchJoin()
            .leftJoin(userQuizAnswer.user, user).fetchJoin()
            .where(
                quiz.serialId.eq(quizSerialId),
                subscription.serialId.eq(subSerialId)
            )
            .fetchOne();

        if (result == null) {
            throw new UserQuizAnswerException(UserQuizAnswerExceptionCode.NOT_FOUND_ANSWER);
        }
        return result;
    }
}