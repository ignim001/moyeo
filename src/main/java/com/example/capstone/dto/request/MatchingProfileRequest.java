package com.example.capstone.dto.request;

import com.example.capstone.entity.TravelStyle;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
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
    private String city;
    private String groupType;
    private String ageRange;
    private List<TravelStyle> travelStyles;
}
