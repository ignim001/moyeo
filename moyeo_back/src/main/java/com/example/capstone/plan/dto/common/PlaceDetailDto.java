package com.example.capstone.plan.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
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
}
