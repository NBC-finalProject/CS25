package com.example.cs25.domain.subscription.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSubscriptionLog is a Querydsl query type for SubscriptionLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSubscriptionLog extends EntityPathBase<SubscriptionLog> {

    private static final long serialVersionUID = -1121922899L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSubscriptionLog subscriptionLog = new QSubscriptionLog("subscriptionLog");

    public final com.example.cs25.global.entity.QBaseEntity _super = new com.example.cs25.global.entity.QBaseEntity(this);

    public final com.example.cs25.domain.quiz.entity.QQuizCategory category;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QSubscription subscription;

    public final NumberPath<Integer> subscriptionType = createNumber("subscriptionType", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSubscriptionLog(String variable) {
        this(SubscriptionLog.class, forVariable(variable), INITS);
    }

    public QSubscriptionLog(Path<? extends SubscriptionLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSubscriptionLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSubscriptionLog(PathMetadata metadata, PathInits inits) {
        this(SubscriptionLog.class, metadata, inits);
    }

    public QSubscriptionLog(Class<? extends SubscriptionLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new com.example.cs25.domain.quiz.entity.QQuizCategory(forProperty("category")) : null;
        this.subscription = inits.isInitialized("subscription") ? new QSubscription(forProperty("subscription"), inits.get("subscription")) : null;
    }

}

