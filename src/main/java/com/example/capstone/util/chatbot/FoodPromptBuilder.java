package com.example.capstone.util.chatbot;

import com.example.capstone.plan.entity.City;
import org.springframework.stereotype.Component;

@Component
public class FoodPromptBuilder {

    public String build(City city) {
        return String.format("""
            너는 대한민국 미식가 여행 전문가야.
                
          [%s] 지역에서 여행자에게 추천할만한 맛집 또는 카페 3곳을 아래 정보와 함께 알려줘.

          각 장소에 대해 JSON 형식으로 제공해줘:

          - name: 상호명
          - menu: 대표 메뉴 (1개)
          - priceRange: 가격대 (예: 10,000원 ~ 15,000원)
          - location: 주소
          - hours: 영업시간

          조건:
          - 반드시 KakaoMap에서 실제 존재하는 상호명을 사용해야 함
          - 출력은 JSON 배열로만 해줘, 설명 없이
          - 장소 수는 정확히 3개
        """, city.getDisplayName());
    }
}
