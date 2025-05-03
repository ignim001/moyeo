package com.example.capstone.util.gpt;

import com.example.capstone.plan.dto.request.TravelPlanRequest;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class GptScheduleStructurePromptBuilder {

    public String build(TravelPlanRequest request) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
        String formattedStart = request.getStartDate().format(formatter);
        String formattedEnd = request.getEndDate().format(formatter);

        sb.append(String.format("%s부터 %s까지 %s로 여행을 떠날 예정이야.\n\n",
                formattedStart,
                formattedEnd,
        sb.append("""
✅ 하루 일정은 반드시 아래 순서를 따라야 해:
1. 아침 (type: 아침)
2. 오전 관광지 또는 액티비티 (type: 관광지 또는 액티비티)
3. 점심 (type: 점심)
4. 오후 관광지 또는 액티비티 (type: 관광지 또는 액티비티)
5. 저녁 (type: 저녁)
6. 야간 관광지 (type: 관광지)
7. 숙소 (type: 숙소)

- 하루마다 이 7가지 일정 항목이 모두 포함되어야 하며, 순서가 바뀌거나 누락되면 안 돼.
- 이 흐름을 모든 날짜마다 반복해서 일정을 구성해줘.
- 하루에 반드시 식사 3회(아침, 점심, 저녁)를 포함해야 해. 누락되지 않도록 주의해.
- 장소 수는 1일 기준 정확히 7개.
- 날짜별로 itinerary.date로 분리해서 구성해줘.

아래 조건을 반영해서 날짜별 여행 일정을 추천해줘.

⚠️ 장소 이름은 다음 기준을 따라줘:

- 관광지나 액티비티인 경우:
  - name 필드에는 사용자가 보기 쉬운 표현을 사용해줘 (예: "한강 자전거 투어", "서울숲 봄 산책")
  - location.name 필드에는 반드시 **KakaoMap에서 검색 가능한 실제 장소명**을 제공해줘.
    - 예: "남산케이블카", "동대문디자인플라자", "북촌한옥마을", "서울숲공원"
    - **띄어쓰기를 모두 제거**하고, 정확한 장소명을 넣어줘. (예: "망원한강공원", "홍익대학교정문")
    - **location.name이 누락되거나 애매하면 안 돼! 반드시 KakaoMap에서 검색 가능한 장소명을 사용해야 해**
  - name과 location.name은 달라도 괜찮아. name은 자유롭게, location.name은 정확하게.

                - 식사(type: 아침, 점심, 저녁)나 숙소(type: 숙소)인 경우:
                 - name 필드는 반드시 **"지역 + 업종" 형태의 키워드**로 구성해줘.
                 - ✅ 예: "성수 파스타 맛집", "이태원 브런치 카페", "종로 이자카야", "홍대 호텔"
                 - ❌ 상호명 사용 금지: (예: "하이디라오", "그랜드 하얏트")
                 - ❌ 감성 형용사 사용 금지: (예: "분위기 좋은", "감성 가득한", "럭셔리한", "아늑한" 등)
                 - 👉 반드시 **KakaoMap에서 검색 가능한 실제 업종 기반 키워드**로 구성해줘.
  - location 필드는 생략하거나 null로 둬도 돼
  - **숙소는 가능한 한 그날 마지막 관광지에서 가까운 지역 기반 숙소로 작성해줘**
""")));

        if (request.getMbti() != null) {
            sb.append("- MBTI: ").append(request.getMbti()).append("\n");
        }
        if (request.getTravelStyle() != null) {
            sb.append("- 여행 성향: ").append(request.getTravelStyle()).append("\n");
        }
        if (request.getPeopleGroup() != null) {
            sb.append("- 여행 인원: ").append(request.getPeopleGroup()).append("명\n");
        }
        if (request.getBudget() != null) {
            sb.append("- 예산: ").append(request.getBudget()).append("원\n");
        }

        sb.append("""

요구사항:
- 하루 3개 이상의 주요 장소 포함 (관광지 또는 활동)
- 계절에 어울리는 장소/활동 포함 (봄 = 벚꽃/산책, 여름 = 해변/계곡, 가을 = 단풍/트레킹, 겨울 = 실내 명소/야경)
- 일정은 사용자 1인 기준
- 각 일정 항목에는 간단한 설명(description)도 함께 포함해줘
- 위도/경도는 절대 포함하지 마 (lat/lng는 null로 넣고, 서버에서 보정할 거야)
- 최근 리뷰가 있는 장소만 사용해줘

관광지 또는 액티비티(type: 관광지, 액티비티)인 경우 반드시 아래 location 정보를 포함해야 해:
- name: 사용자 친화 표현 (예: "경복궁 산책")
- location: {
    name: KakaoMap 검색 가능한 장소명 (띄어쓰기 제거, 예: "경복궁", "남산케이블카"),
    lat: null,
    lng: null
  }

응답 형식:
- 반드시 JSON으로 반환. 다른 문장이나 설명은 절대 추가하지 마.
- 백틱(```) 코드블럭이나 마크다운 양식은 사용하지 마.
- 날짜별 itinerary 배열로 구성
- 각 일정은 'type', 'name', 'description' 필드 포함
- 관광지 또는 액티비티는 반드시 location 포함

예시:
{
  "itinerary": [
    {
      "date": "2025-04-20",
      "schedule": [
       { "type": "아침", "name": "성수 브런치 카페", "description": "달콤한 여유로 시작하는 아침" },
       { "type": "관광지", "name": "서울숲 산책", "location": { "name": "서울숲공원", "lat": null, "lng": null }, "description": "자연 속의 힐링 워킹" },
       { "type": "점심", "name": "성수 파스타 맛집", "description": "부드럽고 향긋한 이탈리안 런치" },
       { "type": "액티비티", "name": "한강 자전거 투어", "location": { "name": "망원한강공원", "lat": null, "lng": null }, "description": "강바람과 함께 두 바퀴" },
       { "type": "저녁", "name": "홍대 이자카야", "description": "도시 속 감성 저녁 한 잔" },
       { "type": "관광지", "name": "청계천 야경 산책", "location": { "name": "청계천", "lat": null, "lng": null }, "description": "불빛과 물소리가 어우러진 밤길" },
       { "type": "숙소", "name": "종로 호텔", "description": "조용한 도심 속 아늑한 휴식" }
       ]
    }
  ]
}
""");

        return sb.toString();
    }
}
