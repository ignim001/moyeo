package com.example.capstone.util.chatbot.recreate;

import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import org.springframework.stereotype.Component;

@Component
public class FoodRecreatePromptBuilder {

    public String build(KakaoPlaceDto place) {
        return String.format("""
[GPT 시스템 명령]

※ 반드시 지켜야 할 출력 조건:
- JSON 객체 1개만 출력 (배열 아님)
- `{`로 시작하고 `}`로 끝나는 순수 JSON만 응답
- 마크다운(예: ```), 설명 문장, 안내 텍스트 절대 포함하지 말 것
- 모든 필드는 null 또는 빈 문자열 없이 자연스럽고 구체적인 한국어로 작성

[사용자 질문]

다음은 실제 존재하는 음식점 정보입니다.
해당 음식점을 기반으로 상세 정보를 작성해주세요.

📍 음식점 정보:
- 상호명: %s
- 주소: %s
- 전화번호: %s
- 카테고리 코드: %s
- 위도: %.6f
- 경도: %.6f

응답 형식:
{
  "name": "상호명 (입력된 그대로)",
  "menu": "대표 메뉴 1~2개",
  "priceRange": "1인당 가격대 (예: 9000원 ~ 12000원)",
  "location": "주소 (입력된 address 그대로 또는 보완 가능)",
  "hours": "영업시간 (예: 매일 10:00 ~ 20:00)"
}
""",
                place.getPlaceName(),
                place.getAddress(),
                place.getPhone() == null ? "정보 없음" : place.getPhone(),
                place.getCategoryGroupCode(),
                place.getLatitude(),
                place.getLongitude());
    }
}
