package com.example.capstone.util.chatbot;

import org.springframework.stereotype.Component;

@Component
public class GPSFoodPromptBuilder {

    public String build(String userQuestion, double latitude, double longitude) {
        return String.format("""
            당신은 대한민국 각 지역의 맛집과 카페 정보를 잘 아는 미식 전문가입니다.

            사용자가 현재 위치를 기준으로 다음과 같은 질문을 했습니다:
            "%s"

            사용자의 위치는 위도 %f, 경도 %f입니다.
            이 질문은 '주변 맛집/카페' 추천에 대한 것입니다.
            사용자의 GPS 위치를 기반으로, 지금 갈 수 있는 맛집이나 카페를 추천해주세요.

            아래 기준을 참고해 구성해주세요:
            - 장소명과 간단한 소개 (음식 종류 포함)
            - 도보 또는 차량 기준 거리 또는 소요 시간 (대략)
            - 분위기나 테마 (조용한, 뷰 좋은, 혼밥 가능 등)
            - 대표 메뉴나 인기 이유

            너무 일반적이거나 블로그 느낌의 과장된 표현은 피하고, 실질적인 맛집/카페 중심으로 알려주세요.
            """, userQuestion, latitude, longitude);
    }
}