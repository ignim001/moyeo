package com.example.capstone.util.chatbot.recreate;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GpsFestivalRecreatePromptBuilder {
    public String build(double lat, double lng, List<String> excludedNames) {
        String excluded = excludedNames.isEmpty() ? "없음" :
                excludedNames.stream().map(n -> "- " + n).collect(Collectors.joining("\n"));

        return String.format("""
[GPT 시스템 명령]

※ 반드시 지켜야 할 출력 조건:
- JSON 배열만 출력 (절대 마크다운 백틱(\\`\\`\\`), 설명, 안내 문장 포함 금지)
- 응답은 JSON 배열로 시작하고 JSON 배열로 끝나야 함
- 각 JSON 객체는 `{`로 시작하고 `}`로 끝나야 함
- 모든 필드는 null, 빈 문자열 없이 반드시 채워야 함

[사용자 질문]

현재 위치: 위도 %.6f, 경도 %.6f

해당 위치 기준 반경 20km 내에서 열리는 축제나 지역 행사 중
아래 '제외 목록'에 있는 축제와 동일하거나 유사한 이름은 절대로 추천하지 마세요.

제외 목록:
%s

추천 조건:
- 2025년 기준 다가오는 실제 축제를 우선 추천
- 플리마켓, 문화 체험 행사, 야외 공연, 박람회, 전시회 등 포함
- KakaoMap에서 검색 가능한 실제 장소에서 열리는 행사만 포함

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

위 형식 그대로 출력하고, 설명 문장, 마크다운 기호, 안내문 없이 JSON만 출력하세요.
""", lat, lng, excluded);
    }
}
