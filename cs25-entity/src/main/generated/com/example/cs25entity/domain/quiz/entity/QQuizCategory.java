package com.example.cs25entity.domain.quiz.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.example.cs25common.global.entity.QBaseEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuizCategory is a Querydsl query type for QuizCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuizCategory extends EntityPathBase<QuizCategory> {

    private static final long serialVersionUID = 795915912L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuizCategory quizCategory = new QQuizCategory("quizCategory");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath categoryType = createString("categoryType");

    public final ListPath<QuizCategory, QQuizCategory> children = this.<QuizCategory, QQuizCategory>createList("children", QuizCategory.class, QQuizCategory.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QQuizCategory parent;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QQuizCategory(String variable) {
        this(QuizCategory.class, forVariable(variable), INITS);
    }

    public QQuizCategory(Path<? extends QuizCategory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuizCategory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuizCategory(PathMetadata metadata, PathInits inits) {
        this(QuizCategory.class, metadata, inits);
    }

    public QQuizCategory(Class<? extends QuizCategory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.parent = inits.isInitialized("parent") ? new QQuizCategory(forProperty("parent"), inits.get("parent")) : null;
    }

}

