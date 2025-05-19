package com.example.capstone.plan.dto.response;

import com.example.capstone.plan.dto.response.FullScheduleResDto.PlaceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEditResDto {
    private int totalEstimatedCost;
    private List<PlaceResponse> places;
}
