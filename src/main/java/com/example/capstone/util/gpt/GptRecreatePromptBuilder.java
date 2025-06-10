package com.example.capstone.util.gpt;

import com.example.capstone.plan.dto.request.ScheduleCreateReqDto;
import com.example.capstone.plan.entity.City;
import com.example.capstone.user.entity.MBTI;
import com.example.capstone.plan.entity.PeopleGroup;
import com.example.capstone.matching.entity.TravelStyle;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class GptRecreatePromptBuilder {

    public String build(ScheduleCreateReqDto request, List<String> excludePlaceNames) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
        String formattedStart = request.getStartDate().format(formatter);
        String formattedEnd = request.getEndDate().format(formatter);
        String destinationText = (request.getDestination() == City.NONE)
                ? "국내"
                : request.getDestination().getDisplayName();

        sb.append("⚠️ 반드시 읽고 지켜야 할 조건이야. 위반 시 전체 응답은 무효야.\n");
        sb.append(String.format("%s부터 %s까지 %s 여행 일정을 다시 생성해줘.\n", formattedStart, formattedEnd, destinationText));

        if (excludePlaceNames != null && !excludePlaceNames.isEmpty()) {
            sb.append("\n[다음 장소는 이미 포함되었으므로 절대 다시 추천하지 마]\n");
            excludePlaceNames.forEach(name -> sb.append("- ").append(name).append("\n"));
            sb.append("- 위 장소 중 하나라도 포함되면 전체 응답은 무효야.\n");
        }

        sb.append("""
[일정 구조 조건]
- 하루 일정은 정확히 다음 7개 항목으로 구성돼야 해. 누락/추가/순서 변경 모두 금지.
1. 아침 (type: 아침)
2. 오전 관광지/액티비티 (type: 관광지 또는 액티비티)
3. 점심 (type: 점심)
4. 오후 관광지/액티비티 (type: 관광지 또는 액티비티)
5. 저녁 (type: 저녁)
6. 야간 관광지 (type: 관광지)
7. 숙소 (type: 숙소)

- 하루 7개 항목이 **정확히 포함되어야 하며**, 하나라도 빠지면 전체 일정이 실패야.
- 모든 날짜에 위 구조를 그대로 반복해.
- GPT는 생성 후 travelSchedule 배열의 길이가 7개인지 스스로 확인해야 해.

[장소명(name) 작성 조건]
- 반드시 **KakaoMap에서 실제로 검색 가능한 상호/장소명만** 사용해야 해.
- 허구 장소, 감성 키워드, 브랜드명은 절대 금지. 예:
  - ❌ 감성 키워드: "감성", "전통", "분위기 좋은", "프리미엄", "럭셔리", "고급", "아늑한"
  - ❌ 허구 조합: "조천휴식공원", "삼양빛오름길", "바다 감성 포차"
  - ❌ 브랜드: "하이디라오", "스타벅스", "그랜드 하얏트"
  - ✅ 예시: "서귀포 브런치 카페", "제주시 고기국수 식당", "중문 호텔"

- 식사/숙소 name은 반드시 "지역 + 업종/음식명 + 식당/호텔/전문점" 형식으로 구성할 것.
- 관광지 및 액티비티는 KakaoMap 기준 실제 장소명이어야 하며 location 필드 포함 (lat/lng=null).

[지역 음식 제한]
- 반드시 **해당 지역에서 실제로 자주 소비되는 음식**만 사용할 것.
  - 예: 제주시 → 고기국수, 흑돼지, 전복죽, 갈치조림, 회 등
  - ❌ 금지 음식: 한우 전골, 중식, 샤브샤브 등 지역 특색이 아닌 메뉴
  
[중복 방지 조건]
- 전체 일정에서 같은 장소(name 기준)가 2번 이상 등장하면 안 돼.
- 장소는 매일 바뀌는 것이 기본이며, **이전 날짜에 등장한 장소는 이후 날짜에서 절대 반복 사용하지 마.**
- name이 조금만 달라 보여도 같은 장소이면 전체 응답은 무효야. GPT가 직접 name을 비교해서 중복 여부를 확인하고 새로 추천해.


[출력 형식]
- 날짜별 itinerary 배열 구성
- 각 일정은 travelSchedule 배열에 포함되며, 각 항목은 type, name, description 포함
- 관광지 또는 액티비티는 location 필드도 포함 (lat, lng는 null)

[응답 검토 필수]
- 제외된 장소가 단 1개라도 포함되면 무효
- 하루 일정 항목 수가 7개가 아니면 무효
- 위 조건 위반 시 전체 JSON 응답은 실패로 간주됨

[예시]
{
  "itinerary": [
    {
      "date": "2025-06-01",
      "travelSchedule": [
        { "type": "아침", "name": "서귀포 브런치 카페", "description": "..." },
        { "type": "관광지", "name": "천지연폭포", "location": { "name": "천지연폭포", "lat": null, "lng": null }, "description": "..." },
        { "type": "점심", "name": "서귀포 고기국수 식당", "description": "..." },
        { "type": "액티비티", "name": "쇠소깍 카약", "location": { "name": "쇠소깍", "lat": null, "lng": null }, "description": "..." },
        { "type": "저녁", "name": "서귀포 흑돼지 식당", "description": "..." },
        { "type": "관광지", "name": "새연교", "location": { "name": "새연교", "lat": null, "lng": null }, "description": "..." },
        { "type": "숙소", "name": "서귀포 호텔", "description": "..." }
      ]
    }
  ]
}
""");

        if (request.getMbti() != MBTI.NONE) sb.append("- MBTI: ").append(request.getMbti()).append("\n");
        if (request.getTravelStyle() != TravelStyle.NONE) sb.append("- 여행 성향: ").append(request.getTravelStyle()).append("\n");
        if (request.getPeopleGroup() != PeopleGroup.NONE) sb.append("- 동행자 유형: ").append(request.getPeopleGroup()).append("\n");
        if (request.getBudget() != null) sb.append("- 예산: ").append(request.getBudget()).append("원\n");

        return sb.toString();
    }


}
