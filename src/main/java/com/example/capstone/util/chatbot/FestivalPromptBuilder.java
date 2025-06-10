package com.example.capstone.util.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class FestivalPromptBuilder {

    /**
     * ✅ GPT 4o용: 축제 1건 단위 프롬프트 (JSON 객체로 응답 강제)
     */
    public String buildSingle(JsonNode item) {
        String name = item.path("title").asText();
        String start = item.path("eventstartdate").asText();
        String end = item.path("eventenddate").asText();
        String period = formatDate(start) + " ~ " + formatDate(end);
        String location = item.path("addr1").asText("");
        String fee = item.path("usetimefestival").asText("무료");

        return String.format("""
            [시스템 역할]
            너는 대한민국 축제 정보를 요약 정리해주는 AI야.

            [제공된 축제 정보]
            {
              "name": "%s",
              "period": "%s",
              "location": "%s",
              "fee": "%s"
            }

            [요청]
            위 축제 정보의 주요 행사 내용 (highlight)을 정확하고 간결하게 1문장으로 작성해줘.

            [출력 형식]
            반드시 아래 JSON 형식 그대로 응답해. 그 외 어떤 문장도 포함하지 말 것:

            {
              "name": "%s",
              "period": "%s",
              "location": "%s",
              "fee": "%s",
              "highlight": "주요 행사 내용"
            }

            [출력 규칙]
            - ✅ JSON 객체 1개만 응답할 것 (배열 금지)
            - ✅ name, period, location, fee 값은 절대 수정하지 말 것
            - ✅ highlight는 1문장으로 명확하고 간결하게 작성
            - ✅ 마크다운, 설명문, 코드블럭, 주석 등은 절대 포함하지 말 것
            """, name, period, location, fee, name, period, location, fee);
    }

    /**
     * ✅ 날짜 형식 변환: yyyyMMdd → yyyy.MM.dd
     */
    private String formatDate(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.length() != 8) return yyyymmdd;
        return yyyymmdd.substring(0, 4) + "." + yyyymmdd.substring(4, 6) + "." + yyyymmdd.substring(6, 8);
    }
}
