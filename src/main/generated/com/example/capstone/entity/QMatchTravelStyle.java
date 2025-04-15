package com.example.capstone.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMatchTravelStyle is a Querydsl query type for MatchTravelStyle
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMatchTravelStyle extends EntityPathBase<MatchTravelStyle> {

    private static final long serialVersionUID = -199786701L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMatchTravelStyle matchTravelStyle = new QMatchTravelStyle("matchTravelStyle");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QMatchingProfile matchingProfile;

    public final EnumPath<TravelStyle> travelStyle = createEnum("travelStyle", TravelStyle.class);

    public QMatchTravelStyle(String variable) {
        this(MatchTravelStyle.class, forVariable(variable), INITS);
    }

    public QMatchTravelStyle(Path<? extends MatchTravelStyle> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMatchTravelStyle(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMatchTravelStyle(PathMetadata metadata, PathInits inits) {
        this(MatchTravelStyle.class, metadata, inits);
    }

    public QMatchTravelStyle(Class<? extends MatchTravelStyle> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.matchingProfile = inits.isInitialized("matchingProfile") ? new QMatchingProfile(forProperty("matchingProfile"), inits.get("matchingProfile")) : null;
    }

}

