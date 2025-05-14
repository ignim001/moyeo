package com.example.capstone.util.chatbot;

import com.example.capstone.plan.entity.City;
import org.springframework.stereotype.Component;

@Component
public class HotelPromptBuilder {

    public String build(City city) {
        return String.format("""
            너는 대한민국 숙소 전문가야.

            [%s] 지역에서 여행자에게 추천할만한 숙소 3곳을 아래 정보와 함께 알려줘.

            각 숙소에 대해 JSON 형식으로 제공해줘:

            - name: 숙소명
            - priceRange: 가격대 (1박 기준, 예: 90,000원 ~ 120,000원)
            - address: 주소
            - phone: 연락처 (없으면 null)
            - checkIn: 체크인 시간
            - checkOut: 체크아웃 시간

            조건:
            - KakaoMap 기준 실제 숙소만 추천해야 함
            - 출력은 JSON 배열로만 해줘, 설명 없이
            - 장소 수는 정확히 3개
        """, city.getDisplayName());
    }
}
