package com.example.capstone.util.chatbot;

import org.springframework.stereotype.Component;

@Component
public class GPSHotelPromptBuilder {

    public String build(String userQuestion, double latitude, double longitude) {
        return String.format("""
            당신은 대한민국 각 지역의 숙소 정보를 잘 아는 여행 숙소 전문가입니다.

            사용자가 현재 위치를 기준으로 다음과 같은 질문을 했습니다:
            "%s"

            사용자의 위치는 위도 %f, 경도 %f입니다.
            이 질문은 '주변 숙소' 추천에 대한 것입니다.
            사용자의 GPS 위치를 기반으로, 지금 이용할 수 있는 숙소를 추천해주세요.

            아래 기준을 참고해 구성해주세요:
            - 숙소명과 간단한 소개 (호텔, 게스트하우스 등)
            - 가격대 또는 이용 팁 (가성비, 고급, 혼자 여행 등)
            - 도보 또는 차량 기준 거리 또는 소요 시간
            - 후기, 편의시설, 뷰(오션뷰, 시티뷰 등) 등 특장점

            광고처럼 느껴지는 표현은 피하고, 신뢰할 수 있는 숙소를 추천해주세요.
            """, userQuestion, latitude, longitude);
    }
}