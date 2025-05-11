package com.example.capstone.plan.service;

import com.example.capstone.plan.dto.common.EditActionDto;
import com.example.capstone.plan.dto.common.PlaceDetailDto;
import com.example.capstone.plan.dto.request.ScheduleEditReqDto;
import com.example.capstone.plan.dto.response.FullScheduleResDto;
import com.example.capstone.plan.dto.response.FullScheduleResDto.DailyScheduleBlock;
import com.example.capstone.plan.dto.response.FullScheduleResDto.PlaceResponse;
import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import com.example.capstone.util.gpt.GptAddPlacePromptBuilder;
import com.example.capstone.util.gpt.GptCostAndTimePromptBuilder;
import com.example.capstone.util.gpt.GptPlaceDescriptionPromptBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ScheduleEditService {

    private final GptCostAndTimePromptBuilder costAndTimePromptBuilder;
    private final GptAddPlacePromptBuilder gptAddPlacePromptBuilder;
    private final GptPlaceDescriptionPromptBuilder gptPlaceDescriptionPromptBuilder;
    private final KakaoMapClient kakaoMapClient;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    public FullScheduleResDto applyEditRequest(ScheduleEditReqDto request) throws Exception {
        List<PlaceDetailDto> currentSchedule = new ArrayList<>(request.getOriginalSchedule());

        for (EditActionDto action : request.getEdits()) {
            switch (action.getAction()) {
                case "add" -> {
                    PlaceDetailDto added = resolveNewPlace(action.getRawInput());
                    currentSchedule.add(action.getIndex(), added);
                }
                case "update" -> {
                    PlaceDetailDto updated = resolveNewPlace(action.getRawInput());
                    currentSchedule.set(action.getIndex(), updated);
                }
                case "delete" -> currentSchedule.remove(action.getIndex());
                case "reorder" -> currentSchedule = reorderList(currentSchedule, action.getFrom(), action.getTo());
                default -> throw new IllegalArgumentException("알 수 없는 액션 타입: " + action.getAction());
            }
        }

        String costPrompt = costAndTimePromptBuilder.build(currentSchedule);
        String gptResponse = openAiClient.callGpt(costPrompt);
        List<PlaceResponse> responses = costAndTimePromptBuilder.parseGptResponse(gptResponse, currentSchedule);
        List<PlaceDetailDto> enrichedPlaces = responses.stream().map(PlaceResponse::toDto).toList();

        List<DailyScheduleBlock> blocks = new ArrayList<>();
        int dayIndex = 0;
        for (int i = 0; i < enrichedPlaces.size(); i += 7) {
            int end = Math.min(i + 7, enrichedPlaces.size());
            List<PlaceDetailDto> subList = enrichedPlaces.subList(i, end);
            int total = subList.stream().map(PlaceDetailDto::getEstimatedCost).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
            List<PlaceResponse> placeResponses = subList.stream().map(PlaceResponse::from).toList();
            blocks.add(new DailyScheduleBlock((dayIndex + 1) + "일차", LocalDate.now().plusDays(dayIndex).toString(), total, placeResponses));
            dayIndex++;
        }

        return new FullScheduleResDto(null, null, null, blocks); // title, startDate, endDate는 null로 처리
    }

    private PlaceDetailDto resolveNewPlace(String rawInputPlaceName) throws Exception {
        String prompt = gptAddPlacePromptBuilder.build(rawInputPlaceName);
        String gptResponse = openAiClient.callGpt(prompt);
        PlaceReplaceResponse parsed = objectMapper.readValue(gptResponse, PlaceReplaceResponse.class);

        KakaoPlaceDto kakao = kakaoMapClient.searchPlace(parsed.getSearchKeyword());
        if (kakao == null) {
            throw new IllegalArgumentException("\u274C 장소를 찾을 수 없습니다: " + parsed.getSearchKeyword());
        }

        PlaceDetailDto place = PlaceDetailDto.builder()
                .name(parsed.getName())
                .type(parsed.getType())
                .address(kakao.getAddress() != null ? kakao.getAddress() : "주소 정보 없음")
                .lat(kakao.getLatitude())
                .lng(kakao.getLongitude())
                .build();

        String descPrompt = gptPlaceDescriptionPromptBuilder.build(List.of(parsed.getName()));
        String descResponse = openAiClient.callGpt(descPrompt);
        Map<String, String> descMap = parseDescriptionResponse(descResponse);
        String desc = findBestMatchingDescription(descMap, parsed.getName());
        place.setDescription(desc != null ? desc : "");

        return place;
    }

    private Map<String, String> parseDescriptionResponse(String raw) {
        Map<String, String> map = new HashMap<>();
        String[] lines = raw.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("-") && line.contains(":")) {
                String[] parts = line.substring(1).split(":", 2);
                if (parts.length == 2) {
                    map.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        return map;
    }

    private String findBestMatchingDescription(Map<String, String> descriptionMap, String targetName) {
        if (descriptionMap.containsKey(targetName)) return descriptionMap.get(targetName);
        for (Map.Entry<String, String> entry : descriptionMap.entrySet()) {
            if (targetName.contains(entry.getKey()) || entry.getKey().contains(targetName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private List<PlaceDetailDto> reorderList(List<PlaceDetailDto> list, int from, int to) {
        List<PlaceDetailDto> newList = new ArrayList<>(list);
        PlaceDetailDto target = newList.remove(from);
        newList.add(to, target);
        return newList;
    }

    private static class PlaceReplaceResponse {
        private String name;
        private String type;
        private String searchKeyword;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getSearchKeyword() { return searchKeyword; }
        public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }
    }
}
