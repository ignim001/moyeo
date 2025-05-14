package com.example.capstone.util.chatbot;

import org.springframework.stereotype.Component;

@Component
public class GPSSpotPromptBuilder {

    public String build(double latitude, double longitude) {
        return String.format("""
            너는 대한민국 관광지 전문 여행 플래너야.

            사용자의 현재 위치는 위도 %.6f, 경도 %.6f야.
            이 위치 근처에서 추천할만한 관광지 3곳을 아래 조건에 맞춰 알려줘.

            각 관광지에 대해 아래 정보를 JSON 형식으로 제공해줘:

            - name: 장소명
            - description: 한줄 설명
            - hours: 운영시간
            - fee: 입장료 (없으면 0원)
            - location: 도로명 주소 또는 지번 주소

            조건:
            - 장소는 KakaoMap에서 실제 존재하는 곳이어야 함
            - 출력은 JSON 배열로만 해줘, 다른 설명이나 코드 블럭(예: ```json)은 포함하지 마.
            - 장소 수는 정확히 3개
        """, latitude, longitude);
    }
}
