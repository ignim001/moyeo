package com.example.capstone.plan.dto.request;

import com.example.capstone.plan.entity.FromPrevious;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ScheduleSaveReqDto {

    private Long userId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DayRequest> days;

    @Data
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class DayRequest {
        private List<PlaceRequest> places;
    }

    @Data
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class PlaceRequest {
        private String name;
        private String type;
        private String address;
        private Double lat;
        private Double lng;
        private String description;
        private Integer estimatedCost;
        private String gptOriginalName;
        private FromPrevious fromPrevious;
        private Integer placeOrder;
    }
}
