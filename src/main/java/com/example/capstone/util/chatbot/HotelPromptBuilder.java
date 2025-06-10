package com.example.capstone.util.chatbot;

import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import org.springframework.stereotype.Component;

@Component
public class HotelPromptBuilder {

    public String build(KakaoPlaceDto place) {
        return String.format("""
            다음은 실제 대한민국에 존재하는 숙소(호텔, 게스트하우스 등)의 정보입니다.

            - 숙소명: %s
            - 주소: %s
            - 전화번호: %s (없다면 빈 문자열로 표시)
            - 카테고리 코드: %s
            - 위도: %f
            - 경도: %f

            {
              "name": "%s",
              "priceRange": "1박 가격대 (예: 90,000원 ~ 120,000원)",
              "address": "%s",
              "phone": "%s (없다면 빈 문자열로 표시)",
              "checkIn": "체크인 시간 (예: 15:00)",
              "checkOut": "체크아웃 시간 (예: 11:00)"
            }

            ⚠️ 반드시 위 JSON 형식 그대로 응답해야 하며
            단순 JSON 객체 하나만 반환하세요.
            """, place.getPlaceName(),
                place.getAddress(),
                place.getPhone() == null ? "" : place.getPhone(),
                place.getCategoryGroupCode(),
                place.getLatitude(),
                place.getLongitude(),
                place.getPlaceName(),
                place.getAddress(),
                place.getPhone() == null ? "" : place.getPhone());
    }
}
