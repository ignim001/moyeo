package com.example.capstone.plan.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GptPlaceDto {
    private String name;
    private String type;
    private Location location;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private String name; // ← 이게 location.name (정제용)
        private Double lat;
        private Double lng;
    }
}
