package com.example.capstone.plan.service;


import com.example.capstone.plan.dto.common.GptPlaceDto;
import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import com.example.capstone.plan.dto.common.PlaceDetailDto;
import com.example.capstone.plan.entity.Day;
import com.example.capstone.plan.entity.Place;
import com.example.capstone.plan.repository.DayRepository;
import com.example.capstone.plan.repository.PlaceRepository;
import com.example.capstone.util.gpt.GptPlaceDescriptionPromptBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.toList;

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
            String gptName = gptPlace.getName(); // ✅ GPT 원본 이름 저장
            System.out.println("✅ GPT 원본 이름: " + gptName); // ✅ 여기에 추가
            String locationName = gptPlace.getLocation() != null ? gptPlace.getLocation().getName() : null;
            String type = normalizeType(gptPlace.getType());
            String categoryCode = mapToCategoryCode(type);

            PlaceDetailDto dto = new PlaceDetailDto();
            dto.setGptOriginalName(gptName);

            if ("관광지".equals(type) || "액티비티".equals(type)) {
                Map<String, Object> location = getAutoLocation(locationName != null ? locationName : gptName);
                if (location != null) {

                    dto.setName((String) location.get("name")); // ✅ 최종 확정된 장소 이름
                    dto.setType(type);
                    dto.setAddress((String) location.get("name"));
                    dto.setLat((Double) location.get("lat"));
                    dto.setLng((Double) location.get("lng"));
                    dto.setDescription(null);
                    dto.setEstimatedCost(null);
                    dto.setFromPrevious(null);

                    dto.setGptOriginalName(gptName); // ✅ 여기 추가 (GPT가 초기에 줬던 이름)

                    refinedPlaces.add(dto);
                }
            }

            // 식사/숙소는 카카오맵 category 검색
            else if ("식사".equals(type) || "숙소".equals(type)) {
                KakaoPlaceDto place = kakaoMapClient.searchPlaceFromGpt(gptName, null, categoryCode);
                if (place != null) {

                    dto.setName(place.getPlaceName());
                    dto.setType(type);
                    dto.setAddress(place.getAddress());
                    dto.setLat(place.getLatitude());
                    dto.setLng(place.getLongitude());
                    dto.setDescription(null);
                    dto.setEstimatedCost(null);
                    dto.setFromPrevious(null);

                    dto.setGptOriginalName(gptName); // ✅ 여기 추가 (GPT가 초기에 줬던 이름)

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
                for (JsonNode dayPlan : itinerary) {
                    JsonNode schedule = dayPlan.get("schedule");
                    if (schedule != null && schedule.isArray()) {
                        for (JsonNode place : schedule) {
                            if (place.has("name") && place.has("type")) {
                                String name = place.get("name").asText();
                                String type = place.get("type").asText();

                                GptPlaceDto.Location location = null;
                                if (place.has("location") && place.get("location").has("name")) {
                                    location = new GptPlaceDto.Location();
                                    location.setName(place.get("location").get("name").asText());
                                    location.setLat(place.get("location").has("lat") ? place.get("location").get("lat").asDouble() : null);
                                    location.setLng(place.get("location").has("lng") ? place.get("location").get("lng").asDouble() : null);
                                }

                                result.add(new GptPlaceDto(name, type, location));
                            }
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

    public Map<String, Object> generateAndRefineScheduleForApi(String prompt) {
        String gptResponse = openAiClient.callGpt(prompt);
        List<PlaceDetailDto> places = getRefinedPlacesFromPrompt(gptResponse);

        Map<String, Object> result = new HashMap<>();
        result.put("originalGptResponse", gptResponse);
        result.put("refinedSchedule", places);

        return result;
    }

    private String extractCoreKeyword(String name) {
        return name.replaceAll("(관람|체험|산책|투어|탐방|감상|방문|구경|트래킹)$", "").trim();
    }


        public List<PlaceDetailDto> getScheduleById(Long scheduleId) {
            // 1. scheduleId에 해당하는 dayId 목록 조회
            List<Long> dayIds = dayRepository.findAllByScheduleId(scheduleId)
                    .stream()
                    .map(Day::getId)
                    .toList();

            if (dayIds.isEmpty()) {
                return List.of(); // 빈 리스트 반환
            }

            // 2. dayId들에 해당하는 장소 조회
            List<Place> places = placeRepository.findAllByDayIdInOrderByDayIdAscPlaceOrderAsc(dayIds);

            // 3. 변환하여 반환
            return places.stream()
                    .map(this::toDto)
                    .toList();
        }

        private PlaceDetailDto toDto(Place place) {
            PlaceDetailDto dto = new PlaceDetailDto();
            dto.setName(place.getName());
            dto.setType(place.getType());
            dto.setAddress(place.getAddress());
            dto.setLat(place.getLat());
            dto.setLng(place.getLng());
            dto.setDescription(place.getDescription());
            dto.setEstimatedCost(place.getEstimatedCost());
            dto.setGptOriginalName(place.getGptOriginalName());
            // fromPrevious는 여기선 없음 (edit 시에 새로 계산됨)
            return dto;
        }
    }



