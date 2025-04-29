package com.example.capstone.matching.dto;

import com.example.capstone.matching.entity.TravelStyle;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MatchingListProfileResDto {

    private String nickname;
    private String imageUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<TravelStyle> travelStyles;
}
