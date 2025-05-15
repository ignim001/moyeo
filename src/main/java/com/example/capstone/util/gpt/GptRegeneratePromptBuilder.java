package com.example.capstone.util.gpt;

import com.example.capstone.plan.dto.request.ScheduleCreateReqDto;
import com.example.capstone.plan.entity.City;
import com.example.capstone.plan.entity.Mbti;
import com.example.capstone.plan.entity.PeopleGroup;
import com.example.capstone.plan.entity.TravelStyle;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class GptRegeneratePromptBuilder {

    public String build(ScheduleCreateReqDto request, List<String> excludePlaceNames) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
        String formattedStart = request.getStartDate().format(formatter);
        String formattedEnd = request.getEndDate().format(formatter);
        String destinationText = (request.getDestination() == City.NONE)
                ? "국내"
                : request.getDestination().getDisplayName();

        // 여행 개요
        sb.append(String.format("%s부터 %s까지 %s로 여행을 떠날 예정이야.\n", formattedStart, formattedEnd, destinationText));

        // 제외 장소
        if (excludePlaceNames != null && !excludePlaceNames.isEmpty()) {
            sb.append("절대 포함하면 안 되는 장소:\n");
            excludePlaceNames.forEach(name -> sb.append("- ").append(name).append("\n"));
        }

        // 일정 구성 규칙
        sb.append("""

✅ 하루 일정은 반드시 다음 7개 항목으로 정확히 구성되어야 해:
- 순서, 항목 수(정확히 7개), 타입(type)을 하나라도 어기면 전체 응답은 무효야.
- GPT가 직접 검증해서 누락 항목이 없는지 확인하고, **각 날짜마다 7개 항목이 모두 포함됐는지 작성 끝에 다시 확인할 것**.

순서:
1. 아침 (type: 아침)
2. 오전 관광지/액티비티 (type: 관광지 또는 액티비티)
3. 점심 (type: 점심)
4. 오후 관광지/액티비티 (type: 관광지 또는 액티비티)
5. 저녁 (type: 저녁)
6. 야간 관광지 (type: 관광지)
7. 숙소 (type: 숙소)

세부 조건:
- 하루 7개 항목은 **정확히 1개씩** 포함되어야 하며, **빠지거나 추가되면 전체 응답이 실패** 처리됨.
- '아침', '점심', '저녁'은 반드시 type으로 정확히 표기.
- 관광지/액티비티는 location.name 필드를 포함해야 하며 KakaoMap 기준 실제 존재하는 장소명 사용.
- lat/lng는 모두 null로 설정 (서버에서 정제 예정).
- 숙소는 마지막 관광지 근처 지역을 기반으로 추천.
- 식사(type: 아침, 점심, 저녁)나 숙소(type: 숙소)인 경우:
 - name 필드는 반드시 **"지역 + 업종" 형태의 키워드**로 구성해줘.
 -  예: "성수 파스타 맛집", "이태원 브런치 카페", "종로 이자카야", "홍대 호텔", "제주 게스트 하우스"
 -  상호명 사용 금지: (예: "하이디라오", "그랜드 하얏트")
 -  감성 형용사 사용 금지: (예: "분위기 좋은", "감성 가득한", "럭셔리한", "아늑한" 등)
 -  반드시 **KakaoMap에서 검색 가능한 실제 업종 기반 키워드**로 구성해줘.


요구사항:
- 하루 3개 이상의 관광지 또는 액티비티 포함
- 계절에 맞는 장소 또는 활동 포함 (봄: 벚꽃/산책, 여름: 해변/계곡, 가을: 단풍/트레킹, 겨울: 실내/야경)
- 일정 수는 반드시 7개로 고정. 누락이 있으면 파싱 불가

JSON 응답은 아래 형식만 반환해. 이외에는 그 어떤 문장, 설명도 포함하지 마:
예시:
 {
   "itinerary": [
     {
       "date": "2025-06-01",
       "travelSchedule": [
         { "type": "아침", "name": "서귀포 브런치 카페", "description": "..." },
         { "type": "관광지", "name": "천지연폭포", "location": { "name": "천지연폭포", "lat": null, "lng": null }, "description": "..." },
         { "type": "점심", "name": "서귀포 고기국수 맛집", "description": "..." },
         { "type": "액티비티", "name": "쇠소깍 카약", "location": { "name": "쇠소깍", "lat": null, "lng": null }, "description": "..." },
         { "type": "저녁", "name": "서귀포 해산물 이자카야", "description": "..." },
         { "type": "관광지", "name": "새연교 야경", "location": { "name": "새연교", "lat": null, "lng": null }, "description": "..." },
         { "type": "숙소", "name": "서귀포 시내 호텔", "description": "..." }
       ]
     }
   ]
 }

조건:
- 위 순서를 하루도 빠짐없이 **정확히 유지**해야 함
- 하루당 **총 7개 일정만 허용**, **누락/추가/순서변경 절대 금지**
- 하루라도 항목이 누락되면 파싱 오류가 발생하니 절대 빠지지 않도록 주의할 것
- 예외는 없으며 위 7개 일정을 **모든 날짜에 동일한 순서로 반복 적용**해야 함
응답 형식:
- 반드시 JSON으로 반환. 다른 문장이나 설명은 절대 추가하지 마
- 백틱(```) 코드블럭이나 마크다운 양식은 사용하지 마
- 날짜별 itinerary 배열로 구성
- 각 일정은 'type', 'name', 'description' 필드 포함
- 관광지 또는 액티비티는 반드시 location 포함

""".formatted(destinationText));

        // 사용자 성향
        if (request.getMbti() != Mbti.NONE) sb.append("- MBTI: ").append(request.getMbti()).append("\n");
        if (request.getTravelStyle() != TravelStyle.NONE) sb.append("- 여행 성향: ").append(request.getTravelStyle()).append("\n");
        if (request.getPeopleGroup() != PeopleGroup.NONE) sb.append("- 동행자 유형: ").append(request.getPeopleGroup()).append("\n");
        if (request.getBudget() != null) sb.append("- 예산: ").append(request.getBudget()).append("원\n");

        return sb.toString();
    }
}
