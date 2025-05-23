package com.example.capstone.util.chatbot;

import com.example.capstone.plan.entity.City;
import org.springframework.stereotype.Component;

@Component
public class FestivalPromptBuilder {

    public String build(City city) {
        return String.format("""
            너는 지역 축제 정보를 잘 아는 여행 가이드야.

            [%s] 지역에서 진행되는 축제나 지역 이벤트 3개를 알려줘.

            각 축제에 대해 아래 정보를 JSON 형식으로 제공해줘:

            - name: 축제명
            - period: 개최 기간 (예: 2025.05.01 ~ 2025.05.07)
            - location: 장소
            - highlight: 주요 행사 내용
            - fee: 입장료 여부 (예: 무료, 10000원)

            조건:
            - 2025년 기준 다가오는 실제 축제를 우선으로 추천
            - KakaoMap에서 장소 검색이 가능한 실제 장소에서 진행되는 행사만 추천
            - 출력은 JSON 배열로만 해줘, 설명 없이
            - 장소 수는 정확히 3개
            절대 아래 사항을 지켜야 합니다:
            - JSON을 반드시 `{` 로 시작하고 `}` 로 끝나는 순수 JSON만 반환하세요.
            - 절대로 마크다운(```) 또는 코드블럭을 사용하지 마세요.
            - 설명 문장, 텍스트, 줄바꿈 없이 JSON만 응답하세요.
        """, city.getDisplayName());
    }
}
