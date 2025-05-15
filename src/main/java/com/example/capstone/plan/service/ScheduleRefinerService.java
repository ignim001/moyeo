package com.example.capstone.plan.service;


import com.example.capstone.plan.dto.common.GptPlaceDto;
import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import com.example.capstone.plan.dto.common.PlaceDetailDto;
import com.example.capstone.plan.entity.TravelDay;
import com.example.capstone.plan.entity.TravelPlace;
import com.example.capstone.plan.repository.DayRepository;
import com.example.capstone.plan.repository.PlaceRepository;
import com.example.capstone.util.gpt.GptPlaceDescriptionPromptBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ScheduleRefinerService {

    private final OpenAiClient openAiClient;
    private final KakaoMapClient kakaoMapClient;
    private final GptPlaceDescriptionPromptBuilder gptPlaceDescriptionPromptBuilder;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DayRepository dayRepository;
    private final PlaceRepository placeRepository;


    /**
     * GPT 응답 JSON에서 정제된 장소 목록을 PlaceDetailDto 형태로 반환
     */
    public List<PlaceDetailDto> getRefinedPlacesFromPrompt(String gptJson) {
        List<GptPlaceDto> gptPlaces = extractPlacesFromGptJson(gptJson);
        List<PlaceDetailDto> refinedPlaces = new ArrayList<>();

        for (GptPlaceDto gptPlace : gptPlaces) {
            String gptName = gptPlace.getName();

            String locationName = gptPlace.getLocation() != null ? gptPlace.getLocation().getName() : null;
            String type = normalizeType(gptPlace.getType());
            String categoryCode = mapToCategoryCode(type);

            // 관광지 또는 액티비티 처리
            if ("관광지".equals(type) || "액티비티".equals(type)) {
                Map<String, Object> location = getAutoLocation(locationName != null ? locationName : gptName);
                if (location != null) {
                    PlaceDetailDto dto = PlaceDetailDto.builder()
                            .name((String) location.get("name"))
                            .type(type)
                            .address((String) location.get("name"))
                            .lat((Double) location.get("lat"))
                            .lng((Double) location.get("lng"))
                            .description(null)
                            .estimatedCost(null)
                            .fromPrevious(null)
                            .gptOriginalName(gptName)
                            .build();

                    refinedPlaces.add(dto);
                }
            }

            // 식사 또는 숙소 처리
            else if ("식사".equals(type) || "숙소".equals(type)) {
                KakaoPlaceDto place = kakaoMapClient.searchPlaceFromGpt(gptName, null, categoryCode);
                if (place != null) {
                    PlaceDetailDto dto = PlaceDetailDto.builder()
                            .name(place.getPlaceName())
                            .type(type)
                            .address(place.getAddress())
                            .lat(place.getLatitude())
                            .lng(place.getLongitude())
                            .description(null)
                            .estimatedCost(null)
                            .fromPrevious(null)
                            .gptOriginalName(gptName)
                            .build();

                    refinedPlaces.add(dto);
                }
            }
        }

        return refinedPlaces;
    }


    private List<GptPlaceDto> extractPlacesFromGptJson(String gptJson) {
        List<GptPlaceDto> result = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(gptJson);
            JsonNode itinerary = root.get("itinerary");

            if (itinerary != null && itinerary.isArray()) {
                for (int i = 0; i < itinerary.size(); i++) {
                    JsonNode dayPlan = itinerary.get(i);
                    String date = dayPlan.get("date").asText();
                    String day = (i + 1) + "일차";
                    JsonNode schedule = dayPlan.has("travelSchedule")
                            ? dayPlan.get("travelSchedule")
                            : dayPlan.get("schedule");

                    if (schedule != null && schedule.isArray()) {
                        for (JsonNode place : schedule) {
                            String name = place.has("name") ? place.get("name").asText() : null;
                            String type = place.has("type") ? place.get("type").asText() : null;

                            GptPlaceDto.Location location = null;
                            if (place.has("location") && place.get("location").has("name")) {
                                location = GptPlaceDto.Location.builder()
                                        .name(place.get("location").get("name").asText())
                                        .lat(place.get("location").has("lat") ? place.get("location").get("lat").asDouble() : null)
                                        .lng(place.get("location").has("lng") ? place.get("location").get("lng").asDouble() : null)
                                        .build();
                            }

                            GptPlaceDto dto = GptPlaceDto.builder()
                                    .name(name)
                                    .type(type)
                                    .location(location)
                                    .build();

                            result.add(dto);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    private String normalizeType(String rawType) {
        if (rawType == null) return "기타";
        return switch (rawType.trim()) {
            case "아침", "점심", "저녁", "브런치", "meal" -> "식사";
            case "숙소", "호텔", "accommodation" -> "숙소";
            case "관광지", "활동", "activity" -> "관광지";
            default -> rawType;
        };
    }

    private String mapToCategoryCode(String type) {
        return switch (normalizeType(type)) {
            case "식사" -> "FD6";
            case "숙소" -> "AD5";
            default -> null;
        };
    }

    private Map<String, Object> getAutoLocation(String placeName) {
        List<String> fallbackKeywords = List.of(
                placeName,
                placeName.replace(" ", ""),
                extractCoreKeyword(placeName),
                extractCoreKeyword(placeName).replace(" ", ""),
                placeName + " 입구",
                placeName + " 주차장"
        );

        for (String keyword : fallbackKeywords) {
            KakaoPlaceDto result = kakaoMapClient.searchPlace(keyword);
            if (result != null) {
                return Map.of(
                        "name", result.getPlaceName(),
                        "lat", result.getLatitude(),
                        "lng", result.getLongitude()
                );
            }
        }

        return null;
    }


    private String extractCoreKeyword(String name) {
        return name.replaceAll("(관람|체험|산책|투어|탐방|감상|방문|구경|트래킹)$", "").trim();
    }


        public List<PlaceDetailDto> getScheduleById(Long scheduleId) {
            // 1. scheduleId에 해당하는 dayId 목록 조회
            List<Long> dayIds = dayRepository.findAllByTravelScheduleId(scheduleId)
                    .stream()
                    .map(TravelDay::getId)
                    .toList();

            if (dayIds.isEmpty()) {
                return List.of(); // 빈 리스트 반환
            }

            // 2. dayId들에 해당하는 장소 조회
            List<TravelPlace> travelPlaces = placeRepository.findAllByTravelDayIdInOrderByTravelDayIdAscPlaceOrderAsc(dayIds);

            // 3. 변환하여 반환
            return travelPlaces.stream()
                    .map(this::toDto)
                    .toList();
        }

    private PlaceDetailDto toDto(TravelPlace travelPlace) {
        return PlaceDetailDto.builder()
                .name(travelPlace.getName())
                .type(travelPlace.getType())
                .address(travelPlace.getAddress())
                .lat(travelPlace.getLat())
                .lng(travelPlace.getLng())
                .description(travelPlace.getDescription())
                .estimatedCost(travelPlace.getEstimatedCost())
                .gptOriginalName(travelPlace.getGptOriginalName())
                .fromPrevious(null) // 필요시 추가
                .build();
    }


}



