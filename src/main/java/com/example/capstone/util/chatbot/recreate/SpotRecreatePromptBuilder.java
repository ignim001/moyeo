package com.example.capstone.util.chatbot.recreate;

import com.example.capstone.plan.entity.City;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpotRecreatePromptBuilder {

    public String build(City city, List<String> excludedNames) {
        String cityKoName = city.getDisplayName();

        String excluded = excludedNames.isEmpty() ? "없음" :
                excludedNames.stream().map(n -> "- " + n).collect(Collectors.joining("\n"));

        return String.format("""
[GPT 시스템 명령]

※ 반드시 지켜야 할 출력 조건:
- JSON 배열만 출력 (절대 마크다운 백틱(```), 설명, 안내 문장 포함 금지)
- 응답은 JSON 배열로 시작하고 JSON 배열로 끝나야 함
- 각 JSON 객체는 `{`로 시작하고 `}`로 끝나야 함
- 모든 필드는 null, 빈 문자열 없이 반드시 채워야 함

[사용자 질문]

도시명: %s

%s에 있는 관광지 중, 아래 '제외 목록'에 있는 장소와 유사하거나 동일한 곳은 절대로 추천하지 마세요.
(예: 지점명, 줄임말, 유사 표현, 프랜차이즈 체인 등 포함)

제외 목록:
%s

추천 조건:
- 지역의 대표 관광지, 자연 명소, 전시공간 등 포함
- KakaoMap에서 검색 가능한 실제 장소만 포함
- 2025년 기준 운영 중인 장소 위주로 추천

응답 형식 (JSON 배열, 정확히 3개):
[
  {
    "name": "장소명",
    "description": "한 문장으로 간단히 소개",
    "hours": "운영시간 (예: 09:00~18:00 또는 상시 개방)",
    "fee": "입장료 (없으면 0원)",
    "location": "도로명 주소 또는 지번 주소"
  },
  ...
]

⚠️ 반드시 JSON만 출력하고, 마크다운 기호, 설명 문장, 안내 텍스트는 절대 포함하지 마세요.
""", cityKoName, cityKoName, excluded);
    }
}
