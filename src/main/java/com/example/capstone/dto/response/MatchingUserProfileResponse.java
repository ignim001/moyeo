package com.example.capstone.dto.response;

import com.example.capstone.entity.CITY;
import com.example.capstone.entity.Gender;
import com.example.capstone.entity.MBTI;
import com.example.capstone.entity.TravelStyle;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MatchingUserProfileResponse {

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
