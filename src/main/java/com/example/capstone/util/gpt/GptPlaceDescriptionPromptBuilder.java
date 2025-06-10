package com.example.capstone.util.gpt;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GptPlaceDescriptionPromptBuilder {

    public String build(List<String> placeNames) {
        StringBuilder sb = new StringBuilder();

        sb.append("다음 장소들에 대해 감성적이고 간결한 한 줄 설명을 만들어줘.\n\n");

        sb.append("장소 목록:\n");
        for (int i = 0; i < placeNames.size(); i++) {
            sb.append(String.format("%d. %s\n", i + 1, placeNames.get(i)));
        }

        sb.append("""
[요청 형식]
아래 장소 리스트에 대해 각각 감성적이고 간결한 한 줄 설명을 작성해주세요.  
JSON 형식의 Key-Value로 반환하되, key는 장소명(String), value는 설명(String)으로 구성해야 합니다.

[반환 예시]
{
  "경복궁": "조선의 숨결을 느낄 수 있는 고궁",
  "청남대": "대통령의 발자취를 따라 걷는 여유로운 산책길"
}

반드시 위 형식의 JSON만 응답하세요. 코드블록, 마크다운, 텍스트 설명은 모두 포함하지 마세요.
""");


        return sb.toString();
    }
}
