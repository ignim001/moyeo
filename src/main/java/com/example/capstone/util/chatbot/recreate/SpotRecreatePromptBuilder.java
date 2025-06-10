package com.example.capstone.util.chatbot.recreate;

import com.example.capstone.plan.entity.City;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpotRecreatePromptBuilder {

    public String build(int index, City city, Double lat, Double lng, List<String> excludedNames) {
        // 제외 목록 포맷
        String excluded = (excludedNames == null || excludedNames.isEmpty())
                ? "없음"
                : excludedNames.stream()
                .map(name -> "- " + name)
                .collect(Collectors.joining("\n"));

        // 위치 정보 설명
        String locationInfo;
        if (city != null) {
            locationInfo = String.format("도시명: %s", city.getDisplayName());
        } else if (lat != null && lng != null) {
            locationInfo = String.format("사용자의 현재 위치는 위도 %.6f, 경도 %.6f입니다.", lat, lng);
        } else {
            throw new IllegalArgumentException("City 또는 위도/경도 정보가 필요합니다.");
        }

        return String.format("""
            [시스템 역할]
            너는 대한민국 관광지 정보를 제공하는 여행 가이드야.

            [요청]
            %s

            아래 제외 목록과 겹치지 않는 관광지 1곳만 추천해줘.
            제외 목록:
            %s

            [응답 형식]
            다음 항목을 포함한 JSON 객체 1개만 반환해줘:
            {
              "name": "장소명",
              "description": "한 문장으로 간단히 소개 (20자 이내)",
              "hours": "운영시간 (예: 09:00~18:00 또는 상시 개방)",
              "fee": "입장료 (없으면 0원)",
              "location": "도로명 주소 또는 지번 주소"
            }

            [주의사항]
            - JSON 외에 그 어떤 텍스트도 출력하지 마
            - 반드시 1개의 JSON **객체만** 출력 (배열 아님)
            - 모든 필드는 null이나 빈 값 없이 정확히 채워야 해
            - 다음 목록에 포함된 장소와 비슷한 이름이나 같은 장소는 절대 추천하지 마: ["한라산", "한라산 국립공원", ...]
            - 예: "한라산"과 "한라산 국립공원"은 같은 장소로 간주됨
                
            """, locationInfo, excluded);
    }
}
