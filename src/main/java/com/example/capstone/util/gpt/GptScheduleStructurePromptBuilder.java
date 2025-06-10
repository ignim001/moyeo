package com.example.capstone.util.gpt;

import com.example.capstone.plan.dto.request.ScheduleCreateReqDto;
import com.example.capstone.plan.entity.City;
import com.example.capstone.user.entity.MBTI;
import com.example.capstone.plan.entity.PeopleGroup;
import com.example.capstone.matching.entity.TravelStyle;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class GptScheduleStructurePromptBuilder {

    public String build(ScheduleCreateReqDto request) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
        String formattedStart = request.getStartDate().format(formatter);
        String formattedEnd = request.getEndDate().format(formatter);

        boolean isDomestic = request.getDestination() == City.NONE;
        String destinationText = isDomestic ? "국내" : request.getDestination().getDisplayName();

        sb.append(String.format("%s부터 %s까지 %s 여행 일정입니다.", formattedStart, formattedEnd, destinationText));

        if (isDomestic) {
            sb.append("""
- 목적지 '국내'일 경우, 계절/MBTI/여행성향/예산/인원 정보를 고려하여 GPT가 적절한 실제 대한민국 도시 중 하나를 **자유롭게 한 곳만 선택**하여 일정을 구성해야 해.
- 단, 하루 또는 전체 일정에서 **두 개 이상의 도시가 섞이면 절대 안 됨.**
- 반드시 **선택된 하나의 도시 내에서만** 전 일정(아침~숙소)을 구성할 것.
""");
        }

        if (request.getMbti() != MBTI.NONE) sb.append("- MBTI: ").append(request.getMbti()).append("\n");
        if (request.getTravelStyle() != TravelStyle.NONE)
            sb.append("- 여행 성향: ").append(request.getTravelStyle()).append("\n");
        if (request.getPeopleGroup() != PeopleGroup.NONE)
            sb.append("- 여행 인원: ").append(request.getPeopleGroup()).append("명\n");
        if (request.getBudget() != null) sb.append("- 예산: ").append(request.getBudget()).append("원\n");

        sb.append("""
[반드시 지켜야 할 규칙]
- 하루 7개 항목(아침, 관광지, 점심, 관광지, 저녁, 관광지, 숙소)로 구성. 누락, 순서 변경, 추가 불가.
- 모든 일정은 반드시 하나의 **시/구 단위** 내에서만 구성되어야 하며, 그 외 지역 포함 시 전체 응답은 무효.

[장소 이름 생성 규칙]

- 숙소:
  - `"지역+동/읍/면+업종"` 형식만 허용 (예: 서귀포시 동홍동 모텔).
  - `"지역+업종"` 또는 감성 표현, 형용사, 브랜드명은 절대 금지 (예: 행복한 쉼터 게스트하우스 ❌).
  - 반드시 **KakaoMap에서 검색 가능한 숙소 명칭 형식**을 따를 것.

- 식사:
  - `"지역+음식종류+식당/전문점"` 형식만 허용 (예: 상주시 삼겹살 식당).
  - 동/읍/면 이름은 절대 포함하지 않음.
  - 다음과 같은 **모호하거나 감성적 키워드**는 절대 금지:
  - “맛집”, “현지인 추천”, “전통”, “조식”, “브런치”, “비건”, “한식 뷔페”, “채식”, “건강식”, “디저트”, “간단한 아침”, “고급”, “감성”
   - 예: “서초구 비건 베이커리 카페”, “강남구 브런치 맛집” 등은 금지
  - 반드시 **해당 지역의 특산물이나 연관된 음식을 기반으로 구성**해야 해.
   - 예: 순창 → 고추장, 장류 요리 / 전주 → 비빔밥 / 통영 → 멍게비빔밥 등
  - 반드시 **KakaoMap에서 검색 가능한 음식점 유형**을 명확히 표현할 것:
   - 예: “강남구 냉면 식당”, “서초구 닭갈비 전문점”, “마포구 곱창 식당”
  - 브랜드명 사용 금지 (예: "이디야", "스타벅스", "배스킨라빈스" ❌)
  - 반드시 `"지역+음식종류+식당/전문점"` 형식 사용 (예: 서초구 일식 전문점, 제주시 흑돼지 식당).
  - 음식종류는 **일반적인 한국식 분류 명칭**만 허용하며, 아래와 같은 표현은 사용 금지:
   - ❌ "일본식", "중국식", "미국식", "비건", "건강식", "가정식", "브런치", "퓨전", "이탈리안", "양식", "분식집", "동남아식" 등
   - ✅ 대신 "일식", "중식", "양식", "분식", "한식", "회", "돈까스", "초밥", "냉면", "갈비", "해장국", "국밥" 등 KakaoMap에 등록된 실존 업종 기반 분류를 사용
  - "맛집", "현지인 추천", "감성", "전통", "조식" 등의 모호한 키워드도 금지

- 관광지 :
  실제 존재하는 관광지 명칭만 사용해야 하며, 다음 조건을 반드시 지켜야 해:
- KakaoMap 또는 NaverMap에 그대로 입력했을 때 검색 가능한 장소명만 사용
- '테마파크', '거리', '마당', '주변', '센터', '명소', '문화공간' 등의 일반 키워드를 포함한 장소명은 무조건 제외
- 관광객이 자주 방문하는 장소여야 하며, 학교, 주민센터, 산업단지, 아파트 단지 등은 제외
- KakaoMap에서 '관광명소', '문화시설', '레저/체험' 카테고리에 속하는 장소만 허용
- 실존하는 장소명만 사용하고, 가공된 이름이나 창작 장소는 절대 포함하지 말 것
- 실제 관광지(건축물, 유적지, 자연경관 등)만 포함
다시 한 번 강조하지만, **지도 앱에서 실제로 검색 가능한 정확한 고유 명칭**만 포함해야 해.
아래 조건을 반드시 지켜서 관광지 이름을 추천해줘:
1. **오직 장소명 단독만 사용해야 해**
   - 예: "천지연폭포", "불국사", "남산타워" (⭕)
   - 예: "강남 대로거리 퍼포먼스", "경복궁 미디어쇼", "홍대 야간 버스킹" (❌)
   - **어떤 활동/수식어/이벤트/공연 이름도 절대 붙이지 말 것**
2. **절대 조합하지 말 것.** 이미 존재하는 장소명 그대로만 사용해.
   - 예: “비룡산자연휴양림” (❌) → “비룡산” (⭕)
3. KakaoMap 또는 NaverMap에서 **그대로 검색 가능한 명칭**만 사용해야 해.
   - “자연휴양림”, “문화체험장” 등을 붙여서 새로운 이름을 만들지 마.
4. 다음과 같은 명칭 조합 또는 표현은 절대 사용하지 마:
   - “~자연휴양림”, "테마파크", "거리", "마당", "주변", "센터", "명소", "공간", "체험장", "쇼", "공연", "퍼포먼스", "축제", "문화", "페스티벌", "존", "스팟", "트레일", "코스", "길"
5. 반드시 실제 관광객이 많이 방문하는 관광지나 문화 유적이어야 해.
6. 지역 커뮤니티 공간(공원, 광장, 기념비 등)은 금지
   - 예: "시비공원", "시민광장", "문학비", "체험마당", "OO쉼터", "작은도서관" 등
7. 주민 대상 공간이 아닌, **관광객이 목적을 갖고 찾아갈 만한 장소**만 포함
8. KakaoMap에 **정확하게 검색되는 명칭이 아니면 무조건 제외**
반드시 **실존 명칭 그대로** 작성 
**창작 금지** 허구의 장소 반환 시 일정 무효 

[JSON 형식 규칙]
- `location` 필드는 관광지에만 포함 (lat/lng는 null 가능).
- `식사`, `숙소` 항목에는 `location` 필드 포함 금지.
- `description`은 반드시 포함하되 빈 문자열로 둘 것 (예: "description": "")
- JSON은 key-value 쌍만 포함하며, 계층 구조 오류나 오타 발생 시 전체 무효.

[중복 방지 조건]
- `name` 기준으로 전체 일정 내 장소 중복 금지.
- 날짜가 달라도 동일 장소/유사 표현 재등장 금지.

[예시]
{
  "itinerary": [
    {
      "date": "2025-06-01",
      "travelSchedule": [
        { "type": "아침", "name": "서귀포시 브런치 식당", "description": "" },
        { "type": "관광지", "name": "천지연폭포", "location": { "name": "천지연폭포", "lat": null, "lng": null }, "description": "" },
        { "type": "점심", "name": "서귀포시 고기국수 식당", "description": "" },
        { "type": "관광지", "name": "쇠소깍 카약", "location": { "name": "쇠소깍", "lat": null, "lng": null }, "description": "" },
        { "type": "저녁", "name": "서귀포시 해산물 포차", "description": "" },
        { "type": "관광지", "name": "새연교 야경", "location": { "name": "새연교", "lat": null, "lng": null }, "description": "" },
        { "type": "숙소", "name": "서귀포시 동홍동 모텔", "description": "" }
      ]
    }
  ]
}
""");


        return sb.toString();
    }
}

