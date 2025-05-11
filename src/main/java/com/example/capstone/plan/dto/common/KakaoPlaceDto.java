package com.example.capstone.plan.dto.common;

import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class KakaoPlaceDto {
    private String placeName;
    private double latitude;
    private double longitude;
    private String address;
    private String categoryGroupCode;
}
