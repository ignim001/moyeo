package com.example.capstone.community.repository;

import com.example.capstone.community.dto.PostListResDto;
import com.example.capstone.community.entity.Post;
import com.example.capstone.matching.entity.City;
import com.example.capstone.matching.entity.Province;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PostRepositoryCustom {
    Slice<PostListResDto> findAllByFilter(Pageable pageable, String title, Province province, City city);
}
