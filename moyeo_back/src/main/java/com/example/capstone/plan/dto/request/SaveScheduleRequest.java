package com.example.capstone.plan.dto.request;


import com.example.capstone.plan.entity.FromPrevious;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SaveScheduleRequest {

    private Long userId;

    private String title;

    private LocalDate startDate;

    private LocalDate endDate;

    private List<DayRequest> days;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DayRequest {
        private Integer dayNumber;
        private List<PlaceRequest> places;
    }

    @Getter
    @Setter
    @NoArgsConstructor
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
