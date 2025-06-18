package com.example.cs25entity.domain.quiz.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QQuizCategory is a Querydsl query type for QuizCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuizCategory extends EntityPathBase<QuizCategory> {

    private static final long serialVersionUID = 795915912L;

    public static final QQuizCategory quizCategory = new QQuizCategory("quizCategory");

    public final com.example.cs25common.global.entity.QBaseEntity _super = new com.example.cs25common.global.entity.QBaseEntity(this);

    public final StringPath categoryType = createString("categoryType");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QQuizCategory(String variable) {
        super(QuizCategory.class, forVariable(variable));
    }

    public QQuizCategory(Path<? extends QuizCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQuizCategory(PathMetadata metadata) {
        super(QuizCategory.class, metadata);
    }

}

