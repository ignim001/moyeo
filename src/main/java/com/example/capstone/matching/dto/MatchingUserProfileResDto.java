package com.example.capstone.matching.dto;

import com.example.capstone.matching.entity.CITY;
import com.example.capstone.user.entity.Gender;
import com.example.capstone.user.entity.MBTI;
import com.example.capstone.matching.entity.TravelStyle;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MatchingUserProfileResDto {

    private String nickname;
    private String imageUrl;
    private Gender gender;
    // 일정
    private LocalDate startDate;
    private LocalDate endDate;
    // 목적지
    private String province;
    private List<CITY> cities;
    private List<TravelStyle> travelStyles;
    private MBTI mbti;

}
