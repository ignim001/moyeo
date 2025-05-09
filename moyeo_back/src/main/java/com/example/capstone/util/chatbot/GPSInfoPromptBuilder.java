package com.example.capstone.util.chatbot;

import org.springframework.stereotype.Component;

@Component
public class GPSInfoPromptBuilder {

    public String build(String userQuestion, double latitude, double longitude) {
        return String.format("""
            당신은 대한민국 관광지 정보에 정통한 여행 전문가입니다.

            사용자가 현재 위치를 기준으로 다음과 같은 질문을 했습니다:
            "%s"

            사용자의 위치는 위도 %f, 경도 %f입니다.
            이 질문은 '주변 관광지' 추천에 대한 것입니다.
            사용자의 GPS 위치를 기반으로, 지금 갈 수 있는 근처 관광 명소를 추천해주세요.

            아래 기준을 참고해 구성해주세요:
            - 장소명과 간단한 소개
            - 도보 또는 차량 기준 거리 또는 소요 시간 (대략)
            - 자연, 문화, 역사, 사진 명소 등 테마에 따른 특징
            - 날씨나 계절에 따라 추천 이유가 있다면 포함

            너무 일반적인 정보는 피하고, 지금 떠날 수 있는 실질적인 장소 중심으로 알려주세요.
            """, userQuestion, latitude, longitude);
    }
}