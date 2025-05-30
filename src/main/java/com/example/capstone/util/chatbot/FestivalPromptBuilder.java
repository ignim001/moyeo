package com.example.capstone.util.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class FestivalPromptBuilder {

    public String build(JsonNode json) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
            [시스템 역할]
            너는 대한민국 축제 정보를 요약 정리해주는 AI 가이드야.

            [요청]
            아래 제공된 축제 정보에서 각 축제의
            1) 주요 행사 내용 (highlight)을 간단하고 정확하게 요약하고,
            2) 입장료 (fee) 항목도 반드시 추정하여 "무료" 또는 "일부 유료" 중 하나로 작성해줘.

            [출력 형식]
            반드시 아래와 같은 JSON 배열 형식으로만 응답해. 예시는 아래와 같아:

            [
              {
                "name": "청주문화제",
                "period": "2025.06.01 ~ 2025.06.05",
                "location": "청주 예술의 전당",
                "fee": "무료",
                "highlight": "지역 전통 예술 공연과 체험행사 중심의 문화제"
              }
            ]

            [출력 규칙 – 반드시 지켜야 함. 위반 시 응답 실패 처리됨]
            1. **응답은 반드시 JSON 배열만 포함**할 것. ✅ 반드시 `[` 로 시작해서 `]` 로 끝나야 함.
            2. ❌ **코드블럭 (` ``` `), 마크다운, 설명 문장, 주석 등은 절대 포함하지 말 것.**
            3. ❌ "다음은 결과입니다", "아래는 요약입니다" 같은 안내 문구도 절대 포함하지 말 것.
            4. ✅ highlight 외의 값(name, period, location, fee)은 절대 수정하지 말고 그대로 유지할 것.
            5. ✅ 모든 필드는 null이나 빈 문자열 없이 반드시 채워져 있어야 함.

            ⚠️⚠️ [절대금지 경고] ⚠️⚠️
            ❌ JSON 외의 어떤 텍스트도 포함하지 말 것.
            ❌ 출력에 ```, "결과:", 마크다운, HTML, 주석, 자연어 설명 등 포함하면 무조건 실패로 처리됨.
            ✅ 반드시 JSON 배열만 출력할 것.

            [제공된 축제 정보]
            """);

        JsonNode items = json.path("response").path("body").path("items").path("item");
        int count = 0;
        for (JsonNode item : items) {
            if (count >= 3) break;

            String name = item.path("title").asText();
            String start = item.path("eventstartdate").asText();
            String end = item.path("eventenddate").asText();
            String period = formatDate(start) + " ~ " + formatDate(end);
            String location = item.path("addr1").asText("");
            String fee = item.path("usetimefestival").asText("무료");

            prompt.append(String.format("""
                - name: %s
                - period: %s
                - location: %s
                - fee: %s

                """, name, period, location, fee));
            count++;
        }

        prompt.append("""
            [요약]
            - 출력은 반드시 JSON 배열만 포함해야 하며, 그 외 어떤 형식도 절대 허용되지 않음.
            - 출력 형식을 어길 경우 응답은 무효 처리됨.
            """);

        return prompt.toString();
    }

    private String formatDate(String yyyymmdd) {
        if (yyyymmdd.length() != 8) return yyyymmdd;
        return yyyymmdd.substring(0, 4) + "." + yyyymmdd.substring(4, 6) + "." + yyyymmdd.substring(6, 8);
    }
}
