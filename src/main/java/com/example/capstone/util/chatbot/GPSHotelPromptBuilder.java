package com.example.capstone.util.chatbot;

import org.springframework.stereotype.Component;

@Component
public class GPSHotelPromptBuilder {
    public String build(double latitude, double longitude) {
        return String.format("""
            너는 대한민국 숙소 추천 전문가야.

            사용자의 현재 위치는 위도 %.6f, 경도 %.6f야.
            이 위치 근처에서 추천할만한 숙소 3곳을 아래 조건에 맞춰 알려줘.

            각 숙소에 대해 아래 정보를 JSON 형식으로 제공해줘:
            - name: 숙소명
            - price: 1박 기준 가격대 (예: 45000원)
            - location: 도로명 주소 또는 지번 주소
            - contact: 연락처
            - checkin_checkout: 체크인/체크아웃 시간

            조건:
            - 장소는 KakaoMap에서 실제 존재하는 곳이어야 함
            - 출력은 JSON 배열로만 해줘, 다른 설명이나 코드 블럭(예: ```json)은 포함하지 마.
            - 장소 수는 정확히 3개
        """, latitude, longitude);
    }
}
