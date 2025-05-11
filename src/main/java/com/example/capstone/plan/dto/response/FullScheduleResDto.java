package com.example.capstone.plan.dto.response;

import com.example.capstone.plan.dto.common.FromPreviousDto;
import com.example.capstone.plan.dto.common.PlaceDetailDto;
import lombok.*;

import java.time.LocalDate;
import java.util.List;



@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FullScheduleResDto {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DailyScheduleBlock> days;

    public List<PlaceDetailDto> toPlaceDetailDtoList() {
        return days.stream()
                .flatMap(day -> day.getPlaces().stream())
                .map(PlaceResponse::toDto)
                .toList();
    }

    @Data
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class DailyScheduleBlock {
        private String day;
        private String date;
        private int totalEstimatedCost;
        private List<PlaceResponse> places;
    }

    @Data
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class PlaceResponse {
        private String name;
        private String gptOriginalName;
        private String type;
        private int estimatedCost;
        private String address;
        private Double lat;
        private Double lng;
        private String description;
        private FromPreviousDto fromPrevious;


        public PlaceDetailDto toDto() {
            return PlaceDetailDto.builder()
                    .name(name)
                    .gptOriginalName(gptOriginalName)
                    .type(type)
                    .estimatedCost(estimatedCost)
                    .address(address)
                    .lat(lat)
                    .lng(lng)
                    .description(description)
                    .fromPrevious(fromPrevious)
                    .build();
        }

        public static PlaceResponse from(PlaceDetailDto dto) {
            return PlaceResponse.builder()
                    .name(dto.getName())
                    .gptOriginalName(dto.getGptOriginalName())
                    .type(dto.getType())
                    .estimatedCost(dto.getEstimatedCost())
                    .address(dto.getAddress())
                    .lat(dto.getLat())
                    .lng(dto.getLng())
                    .description(dto.getDescription())
                    .fromPrevious(dto.getFromPrevious())
                    .build();
        }
    }
}
