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

        // 1. 여행 정보
        sb.append(String.format("%s부터 %s까지 %s 여행 일정.\n", formattedStart, formattedEnd, destinationText));

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

        // 2. 규칙
        sb.append("""
[반드시 지켜야 할 규칙]
- 아래 예시 형식과 **정확히 동일한 JSON 구조**로만 응답하세요.
- **절대 포함하지 말 것**:
  불필요한 문장, 코드블럭(```` 또는 '''), 마크다운, 안내문, 설명, 추가 지시사항, 인사말, 주석, 문단 해설, 감탄사, 헤더, 말머리 등
- 응답에는 **JSON만 순수하게** 포함되어야 하며, 다른 어떤 텍스트도 **앞이나 뒤에 추가하지 말 것**
- JSON 외의 어떤 표현도 포함하면 무효 처리됨
+ - GPT 출력 시작 또는 끝에 ```json, ````, ''', "아래는", "다음은", "이러한", "참고하세요", "예시는", "설명" 등의 단어가 있으면 무효로 간주
+ - GPT가 마크다운 또는 안내문을 자동으로 생성하는 성향을 반드시 억제하고, JSON 외 모든 텍스트 생성을 절대적으로 금지
- 하루 7개 항목(아침, 관광/액티비티, 점심, 관광/액티비티, 저녁, 관광지, 숙소) 누락·순서변경 절대 금지. travelSchedule 배열에 정확히 7개.
- 숙소의 name은 '지역+업종' 키워드만(예: 종로 게스트하우스, 강남 호텔). 상호명, 브랜드, 형용사, 감성표현 모두 금지.
- 식사의 name은 '지역명+음식종류+식당/전문점' 형태로 구성. 예: '서귀포 전복죽 식당', '제주시 흑돼지 전문점'. 모호한 표현 금지.
- 절대 name에 '조식', '맛집', '향토 요리', '현지', '전문점', '카페', '전통' 등의 키워드 단독 사용 금지.
- 서로 다른 지명(예: 함덕+중문)을 하나로 붙이지 마.
- name은 반드시 실제 KakaoMap에서 검색 가능한 **상호명/장소명**만 사용해야 해.
- 예시: "중문 브런치 카페" ✅, "제주시 해산물 포차" ✅
     "중문 프리미엄 카페" ❌, "제주시 바다포차" ❌
- 식사 항목은 반드시 해당 지역(시·구 단위)에서 실제로 유명하거나 흔하게 소비되는 음식 종류만 사용할 것.
예: 제주시 → 흑돼지, 고기국수, 전복죽, 갈치조림, 회 등
- 존재하지만 지역 특색이 아닌 음식(예: '한우 전골', '샤브샤브')은 절대 사용하지 말 것.
- 음식 추천은 관광지 주변에서 실제로 많이 찾는 메뉴 중심으로 구성해.
- 지역명 + 감성어 조합("독립리물멍산책길", "조천휴식공원", "삼양리빛오름길")과 같이 **허구 장소명은 절대 사용하지 마.**
- 실제 존재하는 관광지명(예: "협재해수욕장", "성산일출봉", "도두봉 산책길")만 사용해.(띄어쓰기X, 모호X, 예: 북촌한옥마을, 망원한강공원)**만 사용.
- location 필드는 관광/액티비티만 포함(lat, lng=null). 식사/숙소는 location 생략.
- 날짜별 itinerary 배열, travelSchedule 배열 구조만 생성. description 필드는 간단 설명(생략 가능).
- 오직 key-value만 있는 JSON. key 누락/오타/형식변경 금지.
[중복 방지 조건]
- 전체 일정에서 같은 장소(name 기준)가 2번 이상 등장하면 안 돼.
- 장소는 매일 바뀌는 것이 기본이며, **이전 날짜에 등장한 장소는 이후 날짜에서 절대 반복 사용하지 마.**
- name이 조금만 달라 보여도 같은 장소이면 전체 응답은 무효야. GPT가 직접 name을 비교해서 중복 여부를 확인하고 새로 추천해.

예시:
 {
   "itinerary": [
     {
       "date": "2025-06-01",
       "travelSchedule": [
         { "type": "아침", "name": "중문 브런치 카페", "description": "바다 전망이 있는 브런치 식사" },
         { "type": "관광지", "name": "천지연폭포", "location": { "name": "천지연폭포", "lat": null, "lng": null }, "description": "폭포 경관 감상" },
         { "type": "점심", "name": "서귀포 고기국수 식당", "description": "현지 고기국수 전문점" },
         { "type": "액티비티", "name": "쇠소깍 카약", "location": { "name": "쇠소깍", "lat": null, "lng": null }, "description": "카약 체험" },
         { "type": "저녁", "name": "제주시 해산물 포차", "description": "여러 해산물 요리 제공" },
         { "type": "관광지", "name": "새연교 야경", "location": { "name": "새연교", "lat": null, "lng": null }, "description": "야경 감상" },
         { "type": "숙소", "name": "서귀포 게스트하우스", "description": "편안한 숙박" }
       ]
     }
   ]
 }
""");

        return sb.toString();
    }
}
