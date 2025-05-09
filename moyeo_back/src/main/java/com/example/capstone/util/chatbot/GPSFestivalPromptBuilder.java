package com.example.capstone.util.chatbot;

import org.springframework.stereotype.Component;

@Component
public class GPSFestivalPromptBuilder {

    public String build(String userQuestion, double latitude, double longitude) {
        return String.format("""
            당신은 대한민국 지역 축제, 팝업스토어, 전시회 정보를 잘 아는 문화 이벤트 전문가입니다.

            사용자가 현재 위치를 기준으로 다음과 같은 질문을 했습니다:
            "%s"

            사용자의 위치는 위도 %f, 경도 %f입니다.
            이 질문은 '주변 축제/이벤트' 추천에 대한 것입니다.
            사용자의 GPS 위치를 기반으로, 지금 참여할 수 있는 축제, 전시회, 팝업스토어 등을 추천해주세요.

            아래 기준을 참고해 구성해주세요:
            - 행사명과 간단한 소개
            - 개최 장소 및 위치 정보 (가까운 순)
            - 행사 특징 (야외/실내, 체험형, 먹거리 등)
            - 운영 시간, 주말/주중 여부 등

            지금 실제로 참여 가능한 정보 위주로 간결하게 정리해주세요.
            """, userQuestion, latitude, longitude);
    }
}