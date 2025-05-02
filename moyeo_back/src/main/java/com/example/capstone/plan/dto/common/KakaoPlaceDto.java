package com.example.capstone.plan.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KakaoPlaceDto {
    private String placeName;
    private double latitude;
    private double longitude;
    private String address;
    private String categoryGroupCode;
}
