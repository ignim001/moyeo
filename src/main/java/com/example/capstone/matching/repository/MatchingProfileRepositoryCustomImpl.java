package com.example.capstone.matching.repository;

import com.example.capstone.matching.entity.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.capstone.matching.entity.QMatchCity.matchCity;
import static com.example.capstone.matching.entity.QMatchTravelStyle.matchTravelStyle;
import static com.example.capstone.matching.entity.QMatchingProfile.matchingProfile;
import static com.example.capstone.user.entity.QUserEntity.userEntity;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
public class MatchingProfileRepositoryCustomImpl implements MatchingProfileRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MatchingProfileRepositoryCustomImpl(@Autowired JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<MatchingProfile> matchingProfile(MatchingProfile profile) {
        return queryFactory
                .select(matchingProfile)
                .from(matchingProfile)
                .join(matchingProfile.user, userEntity).fetchJoin()
                .leftJoin(matchingProfile.travelStyles, matchTravelStyle).fetchJoin()
                .leftJoin(matchingProfile.matchCities, matchCity)
                .where(
                        notSelf(profile.getId()),
                        dateBetween(profile.getStartDate(), profile.getEndDate()),
                        provinceEq(profile.getProvince()),
                        cityEq(profile.getMatchCities()),
                        groupTypeEq(profile.getGroupType()),
                        ageRangeEq(profile.getAgeRange()),
                        travelStyleEq(profile.getTravelStyles())
                )
                .fetch();
    }

    private BooleanExpression notSelf(Long profileId) {
        return matchingProfile.id.ne(profileId);
    }

    private BooleanExpression dateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {throw new RuntimeException("input start, end Date");}
        return matchingProfile.startDate.loe(endDate).and(matchingProfile.endDate.goe(startDate));
    }

    private BooleanExpression provinceEq(Province province) {
        return (province != null && province != Province.NONE) ? matchingProfile.province.eq(province) : null;
    }

    private BooleanExpression groupTypeEq(GroupType groupType) {
        return (groupType != null && groupType != GroupType.NONE) ? matchingProfile.groupType.eq(groupType) : null;
    }

    private BooleanExpression ageRangeEq(Integer ageRange) {
        if (ageRange == null) {
            return null;
        }
        switch (ageRange) {
            case 10:
                return userEntity.age.between(10, 19);
            case 20:
                return userEntity.age.between(20, 29);
            case 30:
                return userEntity.age.between(30, 39);
            case 40:
                return userEntity.age.between(40, 49);
            case 50:
                return userEntity.age.between(50, 59);
            case 60:
                return userEntity.age.goe(60);
            default:
                return null;
        }
    }

    private BooleanExpression travelStyleEq(List<MatchTravelStyle> travelStyles) {
        if (travelStyles == null || travelStyles.isEmpty()) {
            return null;
        }
        // NONE 필터 처리
        List<TravelStyle> styles = travelStyles.stream()
                .map(MatchTravelStyle::getTravelStyle)
                .filter(style -> style != null && style != TravelStyle.NONE)
                .collect(Collectors.toList());

        return styles.isEmpty() ? null : matchTravelStyle.travelStyle.in(styles);
    }

    private BooleanExpression cityEq(List<MatchCity> cites) {
        if (cites == null || cites.isEmpty()) {
            return null;
        }
        // NONE 필터 처리
        List<City> cities = cites.stream()
                .map(MatchCity::getCity)
                .filter(city -> city != null && !city.equals(City.NONE))
                .collect(Collectors.toList());

        return cities.isEmpty() ? null : matchCity.city.in(cities);
    }
}
