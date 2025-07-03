package com.example.cs25entity.domain.mail.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMailLog is a Querydsl query type for MailLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMailLog extends EntityPathBase<MailLog> {

    private static final long serialVersionUID = 1206047030L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMailLog mailLog = new QMailLog("mailLog");

    public final StringPath caused = createString("caused");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.example.cs25entity.domain.quiz.entity.QQuiz quiz;

    public final DateTimePath<java.time.LocalDateTime> sendDate = createDateTime("sendDate", java.time.LocalDateTime.class);

    public final EnumPath<com.example.cs25entity.domain.mail.enums.MailStatus> status = createEnum("status", com.example.cs25entity.domain.mail.enums.MailStatus.class);

    public final com.example.cs25entity.domain.subscription.entity.QSubscription subscription;

    public QMailLog(String variable) {
        this(MailLog.class, forVariable(variable), INITS);
    }

    public QMailLog(Path<? extends MailLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMailLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMailLog(PathMetadata metadata, PathInits inits) {
        this(MailLog.class, metadata, inits);
    }

    public QMailLog(Class<? extends MailLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.quiz = inits.isInitialized("quiz") ? new com.example.cs25entity.domain.quiz.entity.QQuiz(forProperty("quiz"), inits.get("quiz")) : null;
        this.subscription = inits.isInitialized("subscription") ? new com.example.cs25entity.domain.subscription.entity.QSubscription(forProperty("subscription"), inits.get("subscription")) : null;
    }

}

