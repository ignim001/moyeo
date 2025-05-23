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

            반드시 지켜야 할 조건:
            - JSON 외 어떤 문장도 포함하지 마세요 (마크다운, 코드블럭, 안내문 등 모두 금지)
            - 응답은 아래 JSON 형식으로 정확히 구성해야 합니다.
            - 각 필드는 반드시 값이 있어야 하며, null 또는 빈 문자열 금지
            - 모든 정보는 최대한 구체적으로 작성하세요

            {
              "name": "%s",
              "menu": "고기국수, 비빔국수", 
              "priceRange": "9,000원 ~ 12,000원",
              "location": "%s",
              "hours": "매일 10:00~20:00"
            }
        ⚠️ 반드시 위 JSON 형식 그대로 응답해야 하며, 다음을 절대 포함하지 마세요:
        - 마크다운(```)
        - 설명 문장
        - 기타 안내 문구

        단순 JSON 객체 하나만 반환하세요.
        """,    place.getPlaceName(),
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
