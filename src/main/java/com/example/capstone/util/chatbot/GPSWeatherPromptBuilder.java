package com.example.capstone.util.chatbot;

import org.springframework.stereotype.Component;

@Component
public class GPSWeatherPromptBuilder {
    public String build(double latitude, double longitude) {
        return String.format("""
            너는 대한민국 기상 전문가야.

            현재 사용자의 위치는 위도 %.6f, 경도 %.6f야.
            이 위치의 날씨 정보를 다음과 같이 알려줘.

            형식은 JSON 객체로 제공해줘:
            - region: 지역명
            - currentTemp: 현재기온 (단위: °C)
            - minMaxTemp: 최저/최고 기온 (예: 최저 12°C / 최고 23°C)
            - rainProb: 강수확률 (단위: %)

            출력은 반드시 JSON 객체로만 해줘, 다른 설명이나 코드 블럭(예: ```json)은 포함하지 마.
        """, latitude, longitude);
    }
}

