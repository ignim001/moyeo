package com.example.capstone.plan.dto.request;

import com.example.capstone.plan.dto.common.PlaceDetailDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlaceUpdateRequest {
    private String originalPlaceName;
    private String newPlaceName;
    private List<PlaceDetailDto> partialSchedule; // 일정 일부
}
