package com.example.capstone.matching.dto;

import com.example.capstone.matching.entity.City;
import com.example.capstone.matching.entity.GroupType;
import com.example.capstone.matching.entity.Province;
import com.example.capstone.matching.entity.TravelStyle;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MatchingProfileReqDto {

    @NotBlank
    private LocalDate startDate;
    @NotBlank
    private LocalDate endDate;

    // 도 단위 지역
    private Province province;
    // 시 단위 지역
    private List<City> cities;
    private GroupType groupType;
    private int ageRange;
    private List<TravelStyle> travelStyles;
}
