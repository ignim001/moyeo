package com.example.capstone.util.gpt;

import com.example.capstone.plan.dto.common.FromPreviousDto;
import com.example.capstone.plan.dto.common.PlaceDetailDto;
import com.example.capstone.plan.dto.response.FullScheduleResDto.PlaceResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GptCostAndTimePromptBuilder {

    public String build(List<PlaceDetailDto> places) {
        StringBuilder sb = new StringBuilder();

        sb.append("너는 한국 여행 전문가이며 여행사 우수 사원이야. 아래는 사용자가 하루 동안 이동할 여행 일정이야.\n\n");
        sb.append("각 장소에 대해 1인 기준 예상 비용(단위: 원)과,\n");
        sb.append("각 장소 사이의 이동 시간(도보/대중교통/차량)을 추정해서 알려줘.\n\n");
        sb.append("- 장소는 KakaoMap 기준으로 실제 존재하는 곳이야.\n");
        sb.append("- 가격은 대략적인 기준으로 알려줘 (식당: 1인 메뉴 / 숙소: 1인 1박 / 관광지: 입장료)\n");
        sb.append("- 각 장소 간 이동 시간은 도보, 대중교통, 차량 기준으로 모두 제공해줘. 단, 실제 소요 시간보다 10분 정도 여유 있게 반영해줘.\n");
        sb.append("- JSON 코드블록 없이 평문 JSON으로만 응답해줘. 백틱(```)이나 마크다운 없이 JSON 그대로 줘.\n\n");

        sb.append("응답 형식 예시:\n");
        sb.append("{\n  \"places\": [\n    {\n      \"name\": \"성수 브런치 카페\",\n      \"type\": \"식사\",\n      \"estimatedCost\": 13000\n    },\n    {\n      \"name\": \"서울숲공원\",\n      \"type\": \"관광지\",\n      \"estimatedCost\": 0,\n      \"fromPrevious\": {\n        \"walk\": 10,\n        \"publicTransport\": 8,\n        \"car\": 5\n      }\n    }\n  ]\n}\n\n");

        sb.append("다음 장소들에 대해 알려줘:\n\n");
        for (PlaceDetailDto place : places) {
            sb.append(String.format("- %s (%s)\n", place.getName(), place.getType()));
        }
        sb.append("\n⚠️ 주의: 반드시 위 예시와 동일한 형식의 JSON만 반환해. 설명 문장 없이 JSON만 출력해줘.\n");

        return sb.toString();
    }

    public List<PlaceResponse> parseGptResponse(String json, List<PlaceDetailDto> baseList) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        if (!root.has("places") || !root.get("places").isArray()) {
            throw new IllegalArgumentException("❌ GPT 응답에 'places' 배열이 없습니다.");
        }

        JsonNode placesNode = root.get("places");



        List<PlaceResponse> result = new ArrayList<>();
        for (int i = 0; i < placesNode.size(); i++) {
            JsonNode p = placesNode.get(i);
            PlaceDetailDto base = baseList.get(i);

            PlaceResponse response = new PlaceResponse();
            response.setName(base.getName());
            response.setType(base.getType());
            response.setAddress(base.getAddress());
            response.setLat(base.getLat());
            response.setLng(base.getLng());
            response.setDescription(base.getDescription());
            response.setGptOriginalName(base.getGptOriginalName());
            response.setEstimatedCost(p.get("estimatedCost").asInt());

            if (p.has("estimatedCost")) {
                response.setEstimatedCost(p.get("estimatedCost").asInt());
            } else {
                response.setEstimatedCost(0); // 또는 -1 등으로 구분
            }

            if (p.has("fromPrevious")) {
                JsonNode t = p.get("fromPrevious");
                FromPreviousDto fromPrevious = new FromPreviousDto(
                        t.has("walk") ? t.get("walk").asInt() : 0,
                        t.has("publicTransport") ? t.get("publicTransport").asInt() : 0,
                        t.has("car") ? t.get("car").asInt() : 0
                );
                response.setFromPrevious(fromPrevious);
            }


            result.add(response);
        }

        return result;
    }
}
