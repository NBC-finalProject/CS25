package com.example.cs25common.global.domain.userQuizAnswer.repository;

import com.example.cs25common.global.domain.quiz.entity.QQuizCategory;
import com.example.cs25common.global.domain.subscription.entity.QSubscription;
import com.example.cs25common.global.domain.userQuizAnswer.dto.UserAnswerDto;
import com.example.cs25common.global.domain.userQuizAnswer.entity.QUserQuizAnswer;
import com.example.cs25common.global.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class UserQuizAnswerCustomRepositoryImpl implements UserQuizAnswerCustomRepository {

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    public UserQuizAnswerCustomRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<UserQuizAnswer> findByUserIdAndCategoryId(Long userId, Long categoryId) {
        QUserQuizAnswer answer = QUserQuizAnswer.userQuizAnswer;
        QSubscription subscription = QSubscription.subscription;
        QQuizCategory category = QQuizCategory.quizCategory;
        //테이블이 세개 싹 조인갈겨

        return queryFactory
            .selectFrom(answer)
            .join(answer.subscription, subscription)
            .join(subscription.category, category)
            .where(
                answer.user.id.eq(userId),
                category.id.eq(categoryId)
            )
            .fetch();
    }

    @Override
    public List<UserAnswerDto> findUserAnswerByQuizId(Long quizId) {
        QUserQuizAnswer userQuizAnswer = QUserQuizAnswer.userQuizAnswer;

        return queryFactory
            .select(Projections.constructor(UserAnswerDto.class, userQuizAnswer.userAnswer))
            .from(userQuizAnswer)
            .where(userQuizAnswer.quiz.id.eq(quizId))
            .fetch();
    }
}