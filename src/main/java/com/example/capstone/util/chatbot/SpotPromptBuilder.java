package com.example.capstone.util.chatbot;

import com.example.capstone.plan.entity.City;
import org.springframework.stereotype.Component;

@Component
public class SpotPromptBuilder {

    public String build(City city) {
        return String.format("""
            너는 대한민국 관광지 전문 여행 플래너야.

            [%s] 지역의 여행자에게 추천할만한 관광지 3곳을 아래 조건에 맞춰 알려줘.

            각 관광지에 대해 아래 정보를 JSON 형식으로 제공해줘:

            - name: 장소명
            - description: 한줄 설명
            - hours: 운영시간
            - fee: 입장료 (없으면 0원)
            - location: 도로명 주소 또는 지번 주소

            조건:
            - 장소는 KakaoMap에서 실제 존재하는 곳이어야 함
            - 출력은 JSON 배열로만 해줘, 설명 없이
            - 장소 수는 정확히 3개
            절대 아래 사항을 지켜야 합니다:
            - JSON을 반드시 `{` 로 시작하고 `}` 로 끝나는 순수 JSON만 반환하세요.
            - 절대로 마크다운(```) 또는 코드블럭을 사용하지 마세요.
            - 설명 문장, 텍스트, 줄바꿈 없이 JSON만 응답하세요.
        """, city.getDisplayName());
    }
}
