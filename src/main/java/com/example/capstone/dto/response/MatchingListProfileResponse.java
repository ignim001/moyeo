package com.example.capstone.dto.response;

import com.example.capstone.entity.TravelStyle;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MatchingListProfileResponse {

    private String nickname;
    private String imageUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<TravelStyle> travelStyles;
}
