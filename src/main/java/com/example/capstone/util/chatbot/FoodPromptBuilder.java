package com.example.capstone.util.chatbot;

import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import org.springframework.stereotype.Component;

@Component
public class FoodPromptBuilder {

    public String build(KakaoPlaceDto place) {
        return String.format("""
            다음은 실제 존재하는 식당 정보입니다.

            - 상호명: %s
            - 주소: %s
            - 전화번호: %s
            - 카테고리 코드: %s
            - 위도: %f
            - 경도: %f

            이 식당의 다음 정보를 알려줘:
            1. 대표 메뉴 (고유 메뉴 1~2개)
            2. 1인당 가격대 (예: 9,000원 ~ 12,000원)
            3. 영업시간 (정기적인 요일별 패턴)

            ⚠️ 반드시 아래 형식의 JSON 객체 **하나만 정확히** 반환하세요.
            - 모든 필드는 반드시 **null/빈 문자열 없이 구체적인 값**을 채워야 합니다.

            응답 형식:
            {
              "name": "%s",
              "menu": "", 
              "priceRange": "",
              "location": "%s",
              "hours": ""
            }
            """,
                place.getPlaceName(),
                place.getAddress(),
                place.getPhone() == null ? "정보 없음" : place.getPhone(),
                place.getCategoryGroupCode(),
                place.getLatitude(),
                place.getLongitude(),
                place.getPlaceName(),
                place.getAddress()
        );
    }
}

