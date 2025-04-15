package com.example.capstone.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMatchingProfile is a Querydsl query type for MatchingProfile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMatchingProfile extends EntityPathBase<MatchingProfile> {

    private static final long serialVersionUID = -953746613L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMatchingProfile matchingProfile = new QMatchingProfile("matchingProfile");

    public final StringPath ageRange = createString("ageRange");

    public final StringPath city = createString("city");

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final StringPath groupType = createString("groupType");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath province = createString("province");

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final ListPath<MatchTravelStyle, QMatchTravelStyle> travelStyles = this.<MatchTravelStyle, QMatchTravelStyle>createList("travelStyles", MatchTravelStyle.class, QMatchTravelStyle.class, PathInits.DIRECT2);

    public final QUserEntity user;

    public QMatchingProfile(String variable) {
        this(MatchingProfile.class, forVariable(variable), INITS);
    }

    public QMatchingProfile(Path<? extends MatchingProfile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMatchingProfile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMatchingProfile(PathMetadata metadata, PathInits inits) {
        this(MatchingProfile.class, metadata, inits);
    }

    public QMatchingProfile(Class<? extends MatchingProfile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUserEntity(forProperty("user"), inits.get("user")) : null;
    }

}

