package com.example.capstone.util.chatbot;

import org.springframework.stereotype.Component;

@Component
public class GPSFestivalPromptBuilder {
    public String build(double latitude, double longitude) {
        return String.format("""
            너는 대한민국 지역 축제 및 이벤트 추천 전문가야.

            사용자의 현재 위치는 위도 %.6f, 경도 %.6f야.
            이 위치 근처에서 진행 중이거나 예정된 축제나 이벤트 3가지를 아래 조건에 맞춰 알려줘.

            각 항목에 대해 아래 정보를 JSON 형식으로 제공해줘:
            - name: 축제명
            - period: 기간
            - location: 장소
            - content: 주요 행사 내용
            - fee: 입장료 여부 (예: 무료, 10000원)

            조건:
            - KakaoMap에서 실제 존재하는 장소 기준으로 알려줘
            - 출력은 JSON 배열로만 해줘, 다른 설명이나 코드 블럭(예: ```json)은 포함하지 마.
            - 행사 수는 정확히 3개
        """, latitude, longitude);
    }
}

