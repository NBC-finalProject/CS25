package com.example.cs25.domain.users.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 1011875888L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUser user = new QUser("user");

    public final com.example.cs25.global.entity.QBaseEntity _super = new com.example.cs25.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final StringPath name = createString("name");

    public final EnumPath<Role> role = createEnum("role", Role.class);

    public final EnumPath<com.example.cs25.domain.oauth.dto.SocialType> socialType = createEnum("socialType", com.example.cs25.domain.oauth.dto.SocialType.class);

    public final com.example.cs25.domain.subscription.entity.QSubscription subscription;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QUser(String variable) {
        this(User.class, forVariable(variable), INITS);
    }

    public QUser(Path<? extends User> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUser(PathMetadata metadata, PathInits inits) {
        this(User.class, metadata, inits);
    }

    public QUser(Class<? extends User> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.subscription = inits.isInitialized("subscription") ? new com.example.cs25.domain.subscription.entity.QSubscription(forProperty("subscription"), inits.get("subscription")) : null;
    }

}

