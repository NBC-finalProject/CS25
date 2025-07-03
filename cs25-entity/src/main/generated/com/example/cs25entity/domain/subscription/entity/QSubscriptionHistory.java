package com.example.cs25entity.domain.subscription.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSubscriptionHistory is a Querydsl query type for SubscriptionHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSubscriptionHistory extends EntityPathBase<SubscriptionHistory> {

    private static final long serialVersionUID = -867963334L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSubscriptionHistory subscriptionHistory = new QSubscriptionHistory("subscriptionHistory");

    public final com.example.cs25entity.domain.quiz.entity.QQuizCategory category;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final QSubscription subscription;

    public final NumberPath<Integer> subscriptionType = createNumber("subscriptionType", Integer.class);

    public final DatePath<java.time.LocalDate> updateDate = createDate("updateDate", java.time.LocalDate.class);

    public QSubscriptionHistory(String variable) {
        this(SubscriptionHistory.class, forVariable(variable), INITS);
    }

    public QSubscriptionHistory(Path<? extends SubscriptionHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSubscriptionHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSubscriptionHistory(PathMetadata metadata, PathInits inits) {
        this(SubscriptionHistory.class, metadata, inits);
    }

    public QSubscriptionHistory(Class<? extends SubscriptionHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new com.example.cs25entity.domain.quiz.entity.QQuizCategory(forProperty("category"), inits.get("category")) : null;
        this.subscription = inits.isInitialized("subscription") ? new QSubscription(forProperty("subscription"), inits.get("subscription")) : null;
    }

}

