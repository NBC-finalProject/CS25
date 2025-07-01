package com.example.cs25entity.domain.quiz.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuiz is a Querydsl query type for Quiz
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuiz extends EntityPathBase<Quiz> {

    private static final long serialVersionUID = 1330421610L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuiz quiz = new QQuiz("quiz");

    public final com.example.cs25common.global.entity.QBaseEntity _super = new com.example.cs25common.global.entity.QBaseEntity(this);

    public final StringPath answer = createString("answer");

    public final QQuizCategory category;

    public final StringPath choice = createString("choice");

    public final StringPath commentary = createString("commentary");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final EnumPath<com.example.cs25entity.domain.quiz.enums.QuizLevel> level = createEnum("level", com.example.cs25entity.domain.quiz.enums.QuizLevel.class);

    public final StringPath question = createString("question");

    public final StringPath serialId = createString("serialId");

    public final EnumPath<com.example.cs25entity.domain.quiz.enums.QuizFormatType> type = createEnum("type", com.example.cs25entity.domain.quiz.enums.QuizFormatType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QQuiz(String variable) {
        this(Quiz.class, forVariable(variable), INITS);
    }

    public QQuiz(Path<? extends Quiz> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuiz(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuiz(PathMetadata metadata, PathInits inits) {
        this(Quiz.class, metadata, inits);
    }

    public QQuiz(Class<? extends Quiz> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new QQuizCategory(forProperty("category"), inits.get("category")) : null;
    }

}

