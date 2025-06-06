package com.example.capstone.util.gpt;

import com.example.capstone.plan.dto.common.PlaceDetailDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GptCostAndTimePromptBuilder {

    public String build(List<PlaceDetailDto> places) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
            너는 한국 여행 전문가이며 여행사 우수 사원이야. 아래는 사용자가 하루 동안 이동할 여행 일정이야.

            각 장소에 대해 1인 기준 예상 비용(단위: 원)과,
            각 장소 사이의 이동 시간(도보/대중교통/차량)을 추정해서 알려줘.

            - 장소는 KakaoMap 기준으로 실제 존재하는 곳이야.
            - 가격은 대략적인 기준으로 알려줘 (식당: 1인 메뉴 / 숙소: 1인 1박 / 관광지: 입장료)
            - 각 장소 간 이동 시간은 도보, 대중교통, 차량 기준으로 모두 제공해줘. 단, 실제 소요 시간보다 10분 정도 여유 있게 반영해줘.
            - 거리와 관계없이 도보 시간도 반드시 계산해줘. 오래 걸리더라도 도보 시간이 0이 되지 않도록 꼭 포함해줘.
            - JSON 코드블록 없이 평문 JSON으로만 응답해줘. 백틱(```)이나 마크다운 없이 JSON 그대로 줘.

            응답 형식 예시:
            {
              "places": [
                {
                  "name": "성수 브런치 카페",
                  "type": "식사",
                  "estimatedCost": 13000
                },
                {
                  "name": "서울숲공원",
                  "type": "관광지",
                  "estimatedCost": 0,
                  "fromPrevious": {
                    "walk": 10,
                    "publicTransport": 8,
                    "car": 5
                  }
                }
              ]
            }

            다음 장소들에 대해 알려줘:

            """);

        for (PlaceDetailDto place : places) {
            sb.append(String.format("- %s (%s)\n", place.getName(), place.getType()));
        }

        sb.append("""
            
            \u26A0\uFE0F 주의: 반드시 위 예시와 동일한 형식의 JSON만 반환해. 설명 문장 없이 JSON만 출력해줘.
            """);

        return sb.toString();
    }
}
