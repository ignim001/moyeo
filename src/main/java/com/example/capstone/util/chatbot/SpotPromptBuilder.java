package com.example.capstone.util.chatbot;

import com.example.capstone.plan.entity.City;
import org.springframework.stereotype.Component;

@Component
public class SpotPromptBuilder {

    public String build(City city, Double latitude, Double longitude) {
        String intro;

        if (city != null) {
            intro = String.format("[%s] 지역의 여행자에게 추천할만한 관광지 3곳을 아래 조건에 맞춰 알려줘.", city.getDisplayName());
        } else if (latitude != null && longitude != null) {
            intro = String.format("사용자의 현재 위치는 위도 %.6f, 경도 %.6f야. 이 위치 근처에서 추천할만한 관광지 3곳을 아래 조건에 맞춰 알려줘.", latitude, longitude);
        } else {
            throw new IllegalArgumentException("City 또는 GPS 좌표 중 하나는 필수입니다.");
        }

        return String.format("""
            [시스템 역할]
            너는 대한민국 관광지 정보를 전문적으로 제공하는 여행 플래너야.

            [질문]
            %s

            [응답 조건]
            아래 정보를 포함한 JSON 객체 3개를 JSON 배열로 반환해줘:

            - name: 장소명
            - description: 한줄 설명
            - hours: 운영시간
            - fee: 입장료 (없으면 0원)
            - location: 도로명 주소 또는 지번 주소

            [추천 조건]
            - KakaoMap에서 실제 검색 가능한 장소만 추천
            - 자연경관, 박물관, 전통시장, 테마파크, 전망대 등 포함 가능

            [출력 형식 예시]
            [
              {
                "name": "한라산 국립공원",
                "description": "제주에서 가장 높은 산, 등산 명소",
                "hours": "09:00 ~ 18:00",
                "fee": "0원",
                "location": "제주특별자치도 제주시 1100로"
              },
              ...
            ]

            [주의사항 – 반드시 지켜야 함]
            - 출력은 오직 JSON 배열만 포함해야 함
            - 절대로 마크다운(```)이나 코드블럭을 사용하지 마세요
            - JSON 바깥에 설명 문장, 줄바꿈, 안내 문구 등을 포함하지 마세요
            - 모든 필드는 빈 문자열이나 null 없이 채워야 함
        """, intro);
    }
}
