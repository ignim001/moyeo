package com.example.capstone.community.repository;

import com.example.capstone.community.dto.PostListResDto;
import com.example.capstone.matching.entity.City;
import com.example.capstone.matching.entity.Province;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static com.example.capstone.community.entity.QComment.*;
import static com.example.capstone.community.entity.QPost.*;
import static com.example.capstone.user.entity.QUserEntity.*;

@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<PostListResDto> findAllByFilter(Pageable pageable, String title, Province province, City city) {
        int pageSize = pageable.getPageSize();
        List<PostListResDto> result = queryFactory
                .select(Projections.constructor(PostListResDto.class,
                        post.user.nickname,
                        post.title,
                        post.createdTime,
                        post.comments.size(),
                        post.imageUris))
                .from(post)
                .leftJoin(post.user, userEntity)
                .where(
                        containTitle(title),
                        provinceEq(province),
                        cityEq(city))
                .offset(pageable.getOffset())
                .limit(pageSize + 1) // hasNext 판별을 위해 1개 더 조회
                .fetch();

        boolean hasNext = result.size() > pageSize;
        if (hasNext) result.remove(pageSize);

        return new SliceImpl<>(result, pageable, hasNext);
    }

    private BooleanExpression containTitle(String title) {
        return (title != null) ? post.title.contains(title) : null;
    }

    private BooleanExpression provinceEq(Province province) {
        return (province != null && province != Province.NONE) ? post.province.eq(province) : null;
    }

    private BooleanExpression cityEq(City city) {
        return (city != null && city != City.NONE) ? post.city.eq(city) : null;
    }
}
