package com.example.capstone.dto.request;

import com.example.capstone.entity.CITY;
import com.example.capstone.entity.TravelStyle;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MatchingProfileRequest {

    @NotBlank
    private LocalDate startDate;
    @NotBlank
    private LocalDate endDate;

    // 도 단위 지역
    private String province;
    // 시 단위 지역
    private List<CITY> cities;
    private String groupType;
    private int ageRange;
    private List<TravelStyle> travelStyles;
}
