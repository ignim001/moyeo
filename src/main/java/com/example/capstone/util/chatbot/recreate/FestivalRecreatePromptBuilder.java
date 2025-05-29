package com.example.capstone.util.chatbot.recreate;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class FestivalRecreatePromptBuilder {

    public String build(JsonNode json) {
        return String.format("""
            [시스템 역할]
            너는 대한민국 축제 정보를 요약 정리해주는 AI 가이드야.

            [요청]
            아래 제공된 축제 정보에서 각 축제의
            1) 주요 행사 내용 (highlight)을 간단하고 정확하게 요약하고,
            2) 입장료 (fee) 항목도 반드시 추정하여 "무료" 또는 "일부 유료" 중 하나로 작성해줘.
            
            [출력 조건]
            - JSON 배열만 출력 (마크다운 백틱, 설명문, 주석 등 금지)
            - 출력은 JSON 배열로 시작하고 JSON 배열로 끝나야 함
            - 모든 필드는 null 또는 빈 문자열 없이 반드시 채워야 함

            [입력 데이터]
            %s

            [출력 형식 예시]
            [
              {
                "name": "청주문화제",
                "period": "2025.06.01 ~ 2025.06.05",
                "location": "청주 예술의 전당",
                "fee": "무료",
                "highlight": "전통 공연과 거리 퍼레이드 중심의 시민 참여형 축제"
              },
              ...
            ]
            """, json.toPrettyString());
    }
}
