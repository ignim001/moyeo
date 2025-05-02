package com.example.capstone.plan.service;


import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import com.example.capstone.plan.dto.common.PlaceDetailDto;
import com.example.capstone.plan.dto.response.ScheduleDetailFullResponse;
import com.example.capstone.util.gpt.GptAddPlacePromptBuilder;
import com.example.capstone.util.gpt.GptCostAndTimePromptBuilder;
import com.example.capstone.util.gpt.GptPlaceDescriptionPromptBuilder;
import com.example.capstone.util.gpt.GptReplacePromptBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceUpdateService {

    private final KakaoMapClient kakaoMapClient;
    private final GptReplacePromptBuilder gptReplacePromptBuilder;
    private final GptPlaceDescriptionPromptBuilder gptPlaceDescriptionPromptBuilder;
    private final GptCostAndTimePromptBuilder costAndTimePromptBuilder;
    private final GptAddPlacePromptBuilder gptAddPlacePromptBuilder;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    /**
     * rawInput 기반으로 장소 하나를 Kakao + GPT 정제해서 반환
     */
    public PlaceDetailDto resolveNewPlace(String rawInputPlaceName) throws Exception {
        // 1. GPT로 장소 이름 정제
        String prompt = gptAddPlacePromptBuilder.build(rawInputPlaceName);
        String gptResponse = openAiClient.callGpt(prompt);
        PlaceReplaceResponse parsed = objectMapper.readValue(gptResponse, PlaceReplaceResponse.class);

        // 2. Kakao 검색
        KakaoPlaceDto kakao = kakaoMapClient.searchPlaceByKeyword(parsed.getSearchKeyword());
        if (kakao == null) {
            throw new IllegalArgumentException("❌ 장소를 찾을 수 없습니다: " + parsed.getSearchKeyword());
        }

        // 3. DTO 생성
        PlaceDetailDto place = new PlaceDetailDto();
        place.setName(parsed.getName());
        place.setType(parsed.getType());
        place.setAddress(kakao.getAddress() != null ? kakao.getAddress() : "주소 정보 없음");
        place.setLat(kakao.getLatitude());
        place.setLng(kakao.getLongitude());

        // 4. 한줄 설명 생성
        String descPrompt = gptPlaceDescriptionPromptBuilder.build(List.of(parsed.getName()));
        String descResponse = openAiClient.callGpt(descPrompt);
        Map<String, String> descMap = parseDescriptionResponse(descResponse);
        String desc = findBestMatchingDescription(descMap, parsed.getName());
        place.setDescription(desc != null ? desc : "");

        return place;
    }

    // 나머지 기존 기능은 그대로 유지

    public ScheduleDetailFullResponse updatePlaceName(Long scheduleId, List<NameUpdateDto> updates, List<PlaceDetailDto> originalSchedule) throws Exception {
        List<PlaceDetailDto> updatedSchedule = new ArrayList<>(originalSchedule);
        List<String> updatedPlaceNames = new ArrayList<>();

        for (NameUpdateDto update : updates) {
            PlaceDetailDto newPlace = resolveNewPlace(update.newPlaceName());
            for (int i = 0; i < updatedSchedule.size(); i++) {
                if (updatedSchedule.get(i).getName().equals(update.originalPlaceName())) {
                    updatedSchedule.set(i, newPlace);
                    updatedPlaceNames.add(newPlace.getName());
                    break;
                }
            }
        }

        return recalculateCostAndTime(updatedSchedule);
    }

    public ScheduleDetailFullResponse addPlace(String userInputPlaceName, int insertIndex, List<PlaceDetailDto> originalSchedule) throws Exception {
        List<PlaceDetailDto> updatedSchedule = new ArrayList<>(originalSchedule);
        PlaceDetailDto newPlace = resolveNewPlace(userInputPlaceName);
        updatedSchedule.add(insertIndex, newPlace);
        return recalculateCostAndTime(updatedSchedule);
    }

    public ScheduleDetailFullResponse deletePlaces(List<String> placeNamesToDelete, List<PlaceDetailDto> originalSchedule) throws Exception {
        List<PlaceDetailDto> updatedSchedule = originalSchedule.stream()
                .filter(place -> !placeNamesToDelete.contains(place.getName()))
                .toList();
        return recalculateCostAndTime(updatedSchedule);
    }

    public ScheduleDetailFullResponse reorderPlaces(List<String> newOrderPlaceNames, List<PlaceDetailDto> originalSchedule) throws Exception {
        List<PlaceDetailDto> updatedSchedule = new ArrayList<>();
        Map<String, PlaceDetailDto> placeMap = originalSchedule.stream()
                .collect(Collectors.toMap(PlaceDetailDto::getName, p -> p));

        for (String name : newOrderPlaceNames) {
            if (placeMap.containsKey(name)) {
                updatedSchedule.add(placeMap.get(name));
            }
        }

        return recalculateCostAndTime(updatedSchedule);
    }

    private ScheduleDetailFullResponse recalculateCostAndTime(List<PlaceDetailDto> updatedSchedule) throws Exception {
        String costPrompt = costAndTimePromptBuilder.build(updatedSchedule);
        String costResponse = openAiClient.callGpt(costPrompt);
        List<ScheduleDetailFullResponse.PlaceResponse> parsed = costAndTimePromptBuilder.parseGptResponse(costResponse, updatedSchedule);
        return new ScheduleDetailFullResponse(parsed);
    }

    private Map<String, String> parseDescriptionResponse(String raw) {
        Map<String, String> map = new HashMap<>();
        String[] lines = raw.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("-") && line.contains(":")) {
                String[] parts = line.substring(1).split(":", 2);
                if (parts.length == 2) {
                    String name = parts[0].trim();
                    String description = parts[1].trim();
                    map.put(name, description);
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

    public record NameUpdateDto(String originalPlaceName, String newPlaceName) {}

    public static class PlaceReplaceResponse {
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
