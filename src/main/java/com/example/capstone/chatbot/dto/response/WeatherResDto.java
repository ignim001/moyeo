package com.example.capstone.chatbot.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResDto {
    private String region;          // 지역명
    private String currentTemp;     // 현재 기온
    private String minTemp;         // 최저 기온
    private String maxTemp;         // 최고 기온
    private String rainProbability; // 강수 확률
    private String weatherDescription;  // 날씨 상태 (맑음, 흐림 등)
    private String requestTime;        // 요청 시간 (예: "2025-05-29 14:35")
}
