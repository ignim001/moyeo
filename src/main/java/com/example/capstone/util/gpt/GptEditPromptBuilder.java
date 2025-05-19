package com.example.capstone.util.gpt;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GptEditPromptBuilder {

    public String build(List<String> names) {
        StringBuilder sb = new StringBuilder();


        sb.append("""
            너는 대한민국 여행 전문가야.

            아래는 사용자가 편집한 여행 일정이야. 각 장소는 이름만 주어져 있어.
            이 중 일부는 모호할 수 있으니, KakaoMap에서 정확히 검색 가능하도록 정제해줘.

            각 장소에 대해 다음 정보를 생성해서 JSON 배열로 알려줘:

            - name: 사용자가 입력한 장소명을 그대로 사용해줘. 
            - 이 값은 그대로 KakaoMap 검색에 사용될 거야.
            - type: 식사 / 숙소 / 관광지 / 액티비티 중에서 판단해서 설정
            - estimatedCost: 1인 기준 예상 비용 (식당: 1인 메뉴 / 숙소: 1인 1박 / 관광지: 입장료), 원 단위
            - description: 장소에 대한 감성적이고 간결한 한 줄 설명
            - fromPrevious: 이전 장소로부터의 이동 시간 (도보, 대중교통, 차량)
            - gptOriginalName: name을 바탕으로 GPT가 생성한 설명 키워드 또는 해시태그형 문장 
              (예: '제주 전망 좋은 백반 맛집')

            반드시 다음 조건을 지켜줘:
            - JSON 외의 설명은 절대 포함하지 말고, 출력은 반드시 평문 JSON 형태로만 할 것
            - 출력은 배열 형식으로, 입력 순서를 유지할 것
            - 각 장소 간 이동 시간은 도보, 대중교통, 차량 기준으로 모두 제공하되, 실제보다 15분 여유 있게 잡아줘.
            - 각 일차의 첫 번째 장소는 이전 장소가 없기 때문에 fromPrevious를 포함하지 마.
            - 이동 시간 값은 너무 반복적이지 않도록 현실적인 거리 차이를 반영해 다양하게 작성해줘.
            - 절대 도보 시간이 차량보다 짧게 나오면 안 돼. 항상 도보 > 차량 > 대중교통 순으로 시간이 길어야 해.
            - 차량이 30분 이상 걸리는 경우, 도보는 100~180분일 수도 있어. 짧게 뭉뚱그리지 마.
            - 거리와 관계없이 도보 시간도 반드시 계산해줘. 도보 시간이 0이 되지 않도록 꼭 포함해줘.
                
                
            - 입력한 장소 개수와 응답하는 JSON 개수는 반드시 1:1로 정확히 일치해야 해. 절대로 일부만 응답하지 마
            - 출력은 반드시 JSON 배열 형태로만 줘. 설명, 안내 문구, 백틱(```), 마크다운 코드블럭은 절대 포함하지 마

            예시:
            [
              {
                "name": "색달식당",
                "type": "식사",
                "estimatedCost": 10000,
                "description": "제주 바다 전망이 좋은 백반 맛집",
                "fromPrevious": { "walk": 12, "publicTransport": 8, "car": 5 },
                "gptOriginalName": "제주 전망 좋은 백반 맛집"
              }
            ]

            다음 장소들을 처리해줘:
            """);
        for (String name : names) {
            sb.append("- ").append(name).append("\n");
        }

        return sb.toString();
    }
}
