package com.example.capstone.plan.dto.common;

import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PlaceDetailDto {
    private String name;
    private String type;
    private String address;
    private Double lat;
    private Double lng;
    private String description;
    private Integer estimatedCost;
    private FromPreviousDto fromPrevious;
    private String gptOriginalName;
    private String date;
    private String day;

}
