package com.example.capstone.plan.service;

import com.example.capstone.plan.dto.common.EditActionDto;
import com.example.capstone.plan.dto.common.PlaceDetailDto;
import com.example.capstone.plan.dto.request.ScheduleEditReqDto;
import com.example.capstone.plan.dto.response.FullScheduleResDto.DailyScheduleBlock;
import com.example.capstone.plan.dto.response.FullScheduleResDto.PlaceResponse;
import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import com.example.capstone.util.gpt.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ScheduleEditService {

    private final GptCostAndTimePromptBuilder costAndTimePromptBuilder;
    private final GptAddPlacePromptBuilder gptAddPlacePromptBuilder;
    private final GptUpdatePromptBuilder gptUpdatePromptBuilder;
    private final GptPlaceDescriptionPromptBuilder gptPlaceDescriptionPromptBuilder;
    private final KakaoMapClient kakaoMapClient;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    public DailyScheduleBlock applyEditToDay(ScheduleEditReqDto request) throws Exception {
        DailyScheduleBlock originalDay = request.getDay();

        List<PlaceDetailDto> flatList = new ArrayList<>();
        if (originalDay.getPlaces() != null) {
            for (PlaceResponse p : originalDay.getPlaces()) {
                flatList.add(p.toDto());
            }
        }

        for (EditActionDto action : request.getEdits()) {
            switch (action.getAction()) {
                case "add" -> {
                    PlaceDetailDto added = resolvePlaceDetailByAdd(action.getRawInput());
                    flatList.add(action.getIndex(), added);
                }
                case "update" -> {
                    PlaceDetailDto original = flatList.get(action.getIndex());
                    PlaceDetailDto updated = resolvePlaceDetailByUpdate(original.getName(), action.getRawInput(), original.getType());
                    flatList.set(action.getIndex(), updated);
                }
                case "delete" -> flatList.remove(action.getIndex());
                case "reorder" -> flatList = reorderList(flatList, action.getFrom(), action.getTo());
                default -> throw new IllegalArgumentException("\u274C 알 수 없는 액션 타입: " + action.getAction());
            }
        }

        String costPrompt = costAndTimePromptBuilder.build(flatList);
        String gptResponse = openAiClient.callGpt(costPrompt);
        List<PlaceResponse> enriched = costAndTimePromptBuilder.parseGptResponse(gptResponse, flatList);

        List<PlaceResponse> updated = new ArrayList<>();
        for (int i = 0; i < flatList.size(); i++) {
            PlaceDetailDto base = flatList.get(i);
            PlaceResponse enrich = enriched.get(i);
            enrich.setName(base.getName());
            enrich.setType(base.getType());
            enrich.setLat(base.getLat());
            enrich.setLng(base.getLng());
            enrich.setAddress(base.getAddress());
            enrich.setGptOriginalName(base.getGptOriginalName());
            updated.add(enrich);
        }

        int total = updated.stream()
                .map(PlaceResponse::getEstimatedCost)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        return new DailyScheduleBlock(
                originalDay.getDay(),
                originalDay.getDate(),
                total,
                updated
        );
    }

    private PlaceDetailDto resolvePlaceDetailByAdd(String input) throws Exception {
        String prompt = gptAddPlacePromptBuilder.build(input);
        String gptResponse = openAiClient.callGpt(prompt);
        return parseAndEnrichPlace(gptResponse);
    }

    private PlaceDetailDto resolvePlaceDetailByUpdate(String originalName, String newName, String typeHint) throws Exception {
        String prompt = gptUpdatePromptBuilder.build(originalName, newName, typeHint);
        String gptResponse = openAiClient.callGpt(prompt);
        return parseAndEnrichPlace(gptResponse);
    }

    private PlaceDetailDto parseAndEnrichPlace(String gptResponse) throws Exception {
        PlaceReplaceResponse parsed = objectMapper.readValue(gptResponse, PlaceReplaceResponse.class);
        KakaoPlaceDto kakao = kakaoMapClient.searchPlace(parsed.getSearchKeyword());
        if (kakao == null) {
            throw new IllegalArgumentException("\u274C 장소를 찾을 수 없습니다: " + parsed.getSearchKeyword());
        }

        PlaceDetailDto place = PlaceDetailDto.builder()
                .name(parsed.getName())
                .type(parsed.getType())
                .address(Optional.ofNullable(kakao.getAddress()).orElse("주소 정보 없음"))
                .lat(kakao.getLatitude())
                .lng(kakao.getLongitude())
                .gptOriginalName(parsed.getName())
                .build();

        String descPrompt = gptPlaceDescriptionPromptBuilder.build(List.of(parsed.getName()));
        String descResponse = openAiClient.callGpt(descPrompt);
        Map<String, String> descMap = parseDescriptionResponse(descResponse);
        place.setDescription(Optional.ofNullable(findBestMatchingDescription(descMap, parsed.getName())).orElse(""));

        String costPrompt = costAndTimePromptBuilder.build(List.of(place));
        String costResponse = openAiClient.callGpt(costPrompt);
        PlaceResponse costEnriched = costAndTimePromptBuilder.parseGptResponse(costResponse, List.of(place)).get(0);
        place.setEstimatedCost(costEnriched.getEstimatedCost());
        place.setFromPrevious(costEnriched.getFromPrevious());

        return place;
    }

    private Map<String, String> parseDescriptionResponse(String raw) {
        Map<String, String> map = new HashMap<>();
        for (String line : raw.split("\n")) {
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
        return descriptionMap.entrySet().stream()
                .filter(e -> targetName.contains(e.getKey()) || e.getKey().contains(targetName))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
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
