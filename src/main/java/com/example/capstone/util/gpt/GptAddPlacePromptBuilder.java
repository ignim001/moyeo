package com.example.capstone.util.gpt;

import org.springframework.stereotype.Component;

@Component
public class GptAddPlacePromptBuilder {

    public String build(String userInputPlaceName) {
        StringBuilder sb = new StringBuilder();

        sb.append("사용자가 입력한 장소명을 기반으로 실제 검색 가능한 장소명을 정리해줘.\n\n");

        sb.append(String.format("- 입력 장소명: \"%s\"\n\n", userInputPlaceName));

        sb.append("""
요청 조건:
- 반드시 실제 존재하는 장소로 보정해줘 (가급적 정확한 명칭으로 변환).
- 장소 유형은 '관광지', '식사', '숙소', '카페', '액티비티' 중 하나로 구분해줘.
- 장소를 검색할 때 사용할 수 있는 검색 키워드도 생성해줘.
- 모든 결과는 JSON 형식으로 출력해줘. (```json 같은 건 붙이지 마.)

응답 예시:
{
  "name": "남산서울타워",
  "type": "관광지",
  "searchKeyword": "남산서울타워"
}
""");

        return sb.toString();
    }
}
