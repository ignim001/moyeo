package com.example.capstone.plan.dto.common;

import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GptPlaceDto {
    private String name;
    private String type;
    private Location location;

    @Data
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Location {
        private String name;  // GPT 응답상 location.name (ex: 서울역)
        private Double lat;
        private Double lng;
    }
}
