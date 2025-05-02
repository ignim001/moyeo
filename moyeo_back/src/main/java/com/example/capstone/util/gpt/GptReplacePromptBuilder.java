package com.example.capstone.util.gpt;

import org.springframework.stereotype.Component;

@Component
public class GptReplacePromptBuilder {

    public String build(String originalName, String newName, String typeHint) {
        StringBuilder sb = new StringBuilder();

        sb.append("기존 장소 '").append(originalName).append("'를 ");
        sb.append("'").append(newName).append("'이라는 장소로 교체하려고 해.\n");

        if (typeHint != null && !typeHint.isBlank()) {
            sb.append("이 장소는 '").append(typeHint).append("' 유형이야.\n");
        }

        sb.append("""
다음 조건에 맞게 이 장소에 대해 정리해줘. 출력은 JSON 형식으로 해줘. (```json 같은 건 붙이지 마)

- name: 장소의 정확한 이름
- type: 장소 유형 (식당, 카페, 숙소, 관광지, 액티비티 중 하나)
- searchKeyword: 실제 장소 검색 시 사용할 키워드

답변은 반드시 JSON 객체 하나만 반환해줘.
""");

        return sb.toString();
    }
}
