package com.example.capstone.plan.service;

import com.example.capstone.plan.dto.common.FromPreviousDto;
import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import com.example.capstone.plan.dto.request.ScheduleRebuildReqDto;
import com.example.capstone.plan.dto.request.ScheduleRebuildReqDto.ScheduleNameBlock;
import com.example.capstone.plan.dto.response.FullScheduleResDto;
import com.example.capstone.plan.dto.response.FullScheduleResDto.DailyScheduleBlock;
import com.example.capstone.plan.dto.response.FullScheduleResDto.PlaceResponse;
import com.example.capstone.util.gpt.GptRebuildPromptBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleRebuildService {

    private final GptRebuildPromptBuilder promptBuilder;
    private final OpenAiClient openAiClient;
    private final KakaoMapClient kakaoMapClient;
    private final ObjectMapper objectMapper;

    public FullScheduleResDto rebuildFullSchedule(ScheduleRebuildReqDto request) throws Exception {
        List<String> names = extractNames(request.getSchedule());
        String prompt = promptBuilder.build(names);
        String gptResponse = openAiClient.callGpt(prompt);
        List<PlaceResponse> rebuiltPlaces = parseGptResponse(gptResponse);

        List<DailyScheduleBlock> rebuiltDays = new ArrayList<>();
        int idx = 0;
        LocalDate currentDate = request.getStartDate();

        for (int i = 0; i < request.getSchedule().size(); i++) {
            ScheduleNameBlock day = request.getSchedule().get(i);
            List<PlaceResponse> rebuiltDayPlaces = new ArrayList<>();
            for (int j = 0; j < day.getNames().size(); j++) {
                rebuiltDayPlaces.add(rebuiltPlaces.get(idx++));
            }
            int total = rebuiltDayPlaces.stream().mapToInt(PlaceResponse::getEstimatedCost).sum();

            rebuiltDays.add(DailyScheduleBlock.builder()
                    .day(day.getDayLabel())
                    .date(currentDate.plusDays(i).toString())
                    .totalEstimatedCost(total)
                    .places(rebuiltDayPlaces)
                    .build());
        }

        return FullScheduleResDto.builder()
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .days(rebuiltDays)
                .build();
    }

    private List<String> extractNames(List<ScheduleNameBlock> days) {
        List<String> result = new ArrayList<>();
        for (ScheduleNameBlock day : days) {
            result.addAll(day.getNames());
        }
        return result;
    }

    private List<PlaceResponse> parseGptResponse(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);

        if (!root.isArray()) {
            throw new IllegalArgumentException("GPT 응답은 JSON 배열이어야 합니다.");
        }

        List<PlaceResponse> result = new ArrayList<>();
        for (JsonNode node : root) {
            String name = node.path("name").asText();
            String gptOriginalName = node.path("gptOriginalName").asText();

            KakaoPlaceDto kakao = kakaoMapClient.searchPlace(name);
            if (kakao == null) {
                throw new IllegalStateException("KakaoMap 검색 실패: " + name);
            }

            PlaceResponse place = PlaceResponse.builder()
                    .name(name)
                    .gptOriginalName(gptOriginalName)
                    .type(node.path("type").asText())
                    .estimatedCost(node.path("estimatedCost").asInt())
                    .description(node.path("description").asText())
                    .fromPrevious(node.has("fromPrevious") ? new FromPreviousDto(
                            node.get("fromPrevious").path("walk").asInt(0),
                            node.get("fromPrevious").path("publicTransport").asInt(0),
                            node.get("fromPrevious").path("car").asInt(0)
                    ) : null)
                    .address(kakao.getAddress())
                    .lat(kakao.getLatitude())
                    .lng(kakao.getLongitude())
                    .build();

            result.add(place);
        }
        return result;
    }
}
