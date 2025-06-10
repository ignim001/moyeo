package com.example.capstone.util.chatbot;

import com.example.capstone.plan.entity.City;
import org.springframework.stereotype.Component;

@Component
public class SpotPromptBuilder {

    public String build(int index, City city, Double latitude, Double longitude) {
        String locationInfo;
        if (city != null) {
            locationInfo = String.format("[%s] 지역의 여행자에게 추천할만한 관광지 3곳을 알려줘.", city.getDisplayName());
        } else if (latitude != null && longitude != null) {
            locationInfo = String.format("사용자의 현재 위치는 위도 %.6f, 경도 %.6f야. 이 위치 근처의 관광지 3곳을 추천해줘.", latitude, longitude);
        } else {
            throw new IllegalArgumentException("City 또는 GPS 좌표 중 하나는 필요합니다.");
        }

        return String.format("""
        [시스템 역할]
        너는 대한민국 관광지 정보를 제공하는 여행 가이드야.

    [요청]
     %s 지역의 여행자에게 추천할 관광지 1곳을 알려줘.
    
     [출력 조건]
     아래 형식과 똑같이 JSON 객체 1개만 응답해. 절대 배열([])이나 설명문, 마크다운 등을 추가하지 마.
     - 이미 언급한 장소와 비슷하거나 동일한 장소는 절대 중복해서 응답하지 마.
     - 예: "한라산"과 "한라산 국립공원"은 같은 장소로 간주됨.
    
     {
       "name": "불국사",
       "description": "경주의 대표적인 유네스코 세계문화유산 사찰",
       "hours": "08:00 ~ 18:00",
       "fee": "성인 6,000원, 청소년 4,000원",
       "location": "경북 경주시 불국로 385"
     }
    
    
        """, locationInfo);
    }
}
