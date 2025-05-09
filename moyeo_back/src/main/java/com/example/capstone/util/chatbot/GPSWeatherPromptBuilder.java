package com.example.capstone.util.chatbot;

import org.springframework.stereotype.Component;

@Component
public class GPSWeatherPromptBuilder {

    public String build(String userQuestion, double latitude, double longitude) {
        return String.format("""
            당신은 대한민국 지역별 날씨에 맞는 여행 정보를 안내하는 기상 전문 도우미입니다.

            사용자가 현재 위치를 기준으로 다음과 같은 질문을 했습니다:
            "%s"

            사용자의 위치는 위도 %f, 경도 %f입니다.
            이 질문은 '날씨 기반 주변 활동 추천'에 대한 것입니다.
            현재 기상 조건을 고려하여 근처에서 할 수 있는 실내외 활동이나 장소를 추천해주세요.

            아래 기준을 참고해 구성해주세요:
            - 활동 또는 장소명과 간단한 설명
            - 날씨에 맞는 이유 (비/바람/더위/추위 등 고려)
            - 도보/차량 기준 거리 또는 접근성
            - 시간대, 계절, 날씨 변화에 따른 유의사항

            날씨와 위치를 함께 고려한 현실적인 추천을 해주세요.
            """, userQuestion, latitude, longitude);
    }
}