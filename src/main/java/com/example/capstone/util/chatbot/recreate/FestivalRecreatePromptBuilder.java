package com.example.capstone.util.chatbot.recreate;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class FestivalRecreatePromptBuilder {

    public String build(JsonNode item) {
        String name = item.path("title").asText();
        String start = item.path("eventstartdate").asText();
        String end = item.path("eventenddate").asText();
        String period = formatDate(start) + " ~ " + formatDate(end);
        String location = item.path("addr1").asText("");
        String fee = item.path("usetimefestival").asText("무료");

        return String.format("""
            [시스템 역할]
            너는 대한민국 축제 정보를 요약 정리해주는 AI야.

            [입력된 축제 정보]
            {
              "name": "%s",
              "period": "%s",
              "location": "%s",
              "fee": "%s"
            }

            [요청]
            위 축제의 주요 행사 내용을 highlight 필드로 작성해줘.  
            다른 필드는 그대로 유지하고, highlight만 추가해.  
            반드시 아래 JSON 형식 그대로 정확히 응답해.

            [응답 형식]
            {
              "name": "%s",
              "period": "%s",
              "location": "%s",
              "fee": "%s",
              "highlight": "주요 행사 내용"
            }

            [응답 규칙]
            - ✅ JSON 객체 하나만 반환해야 함 (배열 금지)
            - ✅ 마크다운, 설명문, 주석, 코드블럭 등은 절대 포함하지 말 것
            - ✅ highlight는 1문장으로 핵심만 담아 작성
            """, name, period, location, fee, name, period, location, fee);
    }

    private String formatDate(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.length() != 8) return yyyymmdd;
        return yyyymmdd.substring(0, 4) + "." + yyyymmdd.substring(4, 6) + "." + yyyymmdd.substring(6, 8);
    }
}
