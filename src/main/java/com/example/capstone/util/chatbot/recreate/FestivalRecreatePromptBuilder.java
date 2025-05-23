package com.example.capstone.util.chatbot.recreate;

import com.example.capstone.plan.entity.City;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FestivalRecreatePromptBuilder {

    public String build(City city, List<String> excludedNames) {
        String cityKoName = city.getDisplayName();

        String excluded = excludedNames.isEmpty() ? "없음" :
                excludedNames.stream().map(n -> "- " + n).collect(Collectors.joining("\n"));

        return String.format("""
[GPT 시스템 명령]

※ 다음 조건을 반드시 따르세요:
- 반드시 JSON 배열만 출력 (앞뒤 설명이나 마크다운 금지)
- 출력 앞뒤에 ``` 등의 마크다운 기호 포함 금지
- 응답은 순수 JSON 배열로 시작해 JSON 배열로 끝나야 함
- 모든 필드는 null, 빈 문자열 없이 작성
- 출력 예외 발생 시에도 JSON 형식을 지켜야 함

[사용자 질문]

현재 지역: %s

다음은 %s에서 열리는 축제 또는 지역 행사에 대한 추천 요청입니다.
단, 아래 '제외 목록'에 있는 이름과 유사하거나 동일한 축제는 절대로 추천하지 마세요.

제외 목록:
%s

축제 추천 조건:
- 2025년 기준 다가오는 실제 축제를 우선 추천
- 플리마켓, 문화 체험 행사, 야외 공연, 박람회, 전시회 등 포함
- 실제 장소에서 열리고 KakaoMap에서 검색 가능한 행사만 포함

응답 형식 (JSON 배열, 정확히 3개):
[
  {
    "name": "축제명",
    "period": "2025-08-01 ~ 2025-08-03",
    "location": "장소명",
    "highlight": "주요 행사 요약 (1줄)",
    "fee": "무료 or 10000원"
  },
  ...
]

위와 같은 JSON만 출력하세요. 설명문, 줄바꿈, 마크다운 금지.
""", cityKoName, cityKoName, excluded);
    }
}
