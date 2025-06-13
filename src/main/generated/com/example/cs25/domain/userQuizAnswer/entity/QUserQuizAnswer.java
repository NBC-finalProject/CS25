package com.example.cs25.domain.userQuizAnswer.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserQuizAnswer is a Querydsl query type for UserQuizAnswer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserQuizAnswer extends EntityPathBase<UserQuizAnswer> {

    private static final long serialVersionUID = 256811225L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserQuizAnswer userQuizAnswer = new QUserQuizAnswer("userQuizAnswer");

    public final com.example.cs25.global.entity.QBaseEntity _super = new com.example.cs25.global.entity.QBaseEntity(this);

    public final StringPath aiFeedback = createString("aiFeedback");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isCorrect = createBoolean("isCorrect");

    public final com.example.cs25.domain.quiz.entity.QQuiz quiz;

    public final com.example.cs25.domain.subscription.entity.QSubscription subscription;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.example.cs25.domain.users.entity.QUser user;

    public final StringPath userAnswer = createString("userAnswer");

    public QUserQuizAnswer(String variable) {
        this(UserQuizAnswer.class, forVariable(variable), INITS);
    }

    public QUserQuizAnswer(Path<? extends UserQuizAnswer> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserQuizAnswer(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserQuizAnswer(PathMetadata metadata, PathInits inits) {
        this(UserQuizAnswer.class, metadata, inits);
    }

    public QUserQuizAnswer(Class<? extends UserQuizAnswer> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.quiz = inits.isInitialized("quiz") ? new com.example.cs25.domain.quiz.entity.QQuiz(forProperty("quiz"), inits.get("quiz")) : null;
        this.subscription = inits.isInitialized("subscription") ? new com.example.cs25.domain.subscription.entity.QSubscription(forProperty("subscription"), inits.get("subscription")) : null;
        this.user = inits.isInitialized("user") ? new com.example.cs25.domain.users.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

