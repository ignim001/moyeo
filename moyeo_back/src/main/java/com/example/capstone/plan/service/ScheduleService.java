package com.example.capstone.plan.service;


import com.example.capstone.plan.dto.common.FromPreviousDto;
import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import com.example.capstone.plan.dto.common.PlaceDetailDto;
import com.example.capstone.plan.dto.request.PlaceUpdateRequest;
import com.example.capstone.plan.dto.request.SaveScheduleRequest;
import com.example.capstone.plan.dto.request.TravelPlanRequest;
import com.example.capstone.plan.dto.response.DaySummaryResponse;
import com.example.capstone.plan.dto.response.SaveScheduleResponse;
import com.example.capstone.plan.dto.response.ScheduleDetailFullResponse;
import com.example.capstone.plan.dto.response.ScheduleSimpleResponse;
import com.example.capstone.plan.entity.Day;
import com.example.capstone.plan.entity.FromPrevious;
import com.example.capstone.plan.entity.Place;
import com.example.capstone.plan.entity.Schedule;
import com.example.capstone.plan.repository.DayRepository;
import com.example.capstone.plan.repository.PlaceRepository;
import com.example.capstone.plan.repository.ScheduleRepository;
import com.example.capstone.util.gpt.GptCostAndTimePromptBuilder;
import com.example.capstone.util.gpt.GptPlaceDescriptionPromptBuilder;
import com.example.capstone.util.gpt.GptScheduleStructurePromptBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRefinerService scheduleRefinerService;
    private final GptScheduleStructurePromptBuilder structurePromptBuilder;
    private final GptPlaceDescriptionPromptBuilder descriptionPromptBuilder;
    private final GptCostAndTimePromptBuilder costAndTimePromptBuilder;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KakaoMapClient kakaoMapClient;
    private final ScheduleRepository scheduleRepository;
    private final DayRepository dayRepository;
    private final PlaceRepository placeRepository;

    // ✅ 전체 일정 생성 흐름 (일정 + 설명 + 예산 + 이동시간)
    public ScheduleDetailFullResponse generateFullSchedule(TravelPlanRequest request) throws Exception {
        // 1. 일정 생성 프롬프트 → GPT 호출
        String schedulePrompt = structurePromptBuilder.build(request);
        String gptResponse = openAiClient.callGpt(schedulePrompt);

        // 2. 정제된 장소 리스트 추출
        List<PlaceDetailDto> places = scheduleRefinerService.getRefinedPlacesFromPrompt(gptResponse);


        // 3. 각 장소 한줄 설명 생성
        List<String> placeNames = places.stream().map(PlaceDetailDto::getName).toList();
        String descPrompt = descriptionPromptBuilder.build(placeNames);
        String descResponse = openAiClient.callGpt(descPrompt);
        var descriptionMap = parseDescriptionMap(descResponse);

        for (PlaceDetailDto place : places) {
            String desc = descriptionMap.get(place.getName());
            if (desc != null) place.setDescription(desc);
        }

        // 4. 예산/이동시간 추정 요청
        String costPrompt = costAndTimePromptBuilder.build(places);
        String costResponse = openAiClient.callGpt(costPrompt);
        return new ScheduleDetailFullResponse(costAndTimePromptBuilder.parseGptResponse(costResponse, places));
    }

    // ✅ 문자열(JSON)로 반환하는 테스트용 메서드
    public String generateFullScheduleAsString(String prompt) {
        try {
            List<PlaceDetailDto> places = scheduleRefinerService.getRefinedPlacesFromPrompt(prompt);

            // 설명
            List<String> placeNames = places.stream().map(PlaceDetailDto::getName).toList();
            String descPrompt = descriptionPromptBuilder.build(placeNames);
            String descResponse = openAiClient.callGpt(descPrompt);
            var descriptionMap = parseDescriptionMap(descResponse);
            for (PlaceDetailDto place : places) {
                String desc = descriptionMap.get(place.getName());
                if (desc != null) place.setDescription(desc);
            }

            // 예산 및 이동시간
            String costPrompt = costAndTimePromptBuilder.build(places);
            String costResponse = openAiClient.callGpt(costPrompt);

            return objectMapper.writeValueAsString(
                    new ScheduleDetailFullResponse(costAndTimePromptBuilder.parseGptResponse(costResponse, places))
            );
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    public List<PlaceDetailDto> getRefinedPlaces(Long scheduleId) {
        return List.of(); // DB 연동 후 구현 예정
    }

    public List<PlaceDetailDto> getRefinedPlacesFromPrompt(String gptScheduleJson) {
        return scheduleRefinerService.getRefinedPlacesFromPrompt(gptScheduleJson);
    }

    public ScheduleDetailFullResponse getFullDetailFromPrompt(String gptScheduleJson) throws Exception {
        List<PlaceDetailDto> places = scheduleRefinerService.getRefinedPlacesFromPrompt(gptScheduleJson);
        String prompt = costAndTimePromptBuilder.build(places);
        String gptResponse = openAiClient.callGpt(prompt);
        return new ScheduleDetailFullResponse(costAndTimePromptBuilder.parseGptResponse(gptResponse, places));
    }

    public Map<String, Object> generateAndRefineScheduleForApi(String prompt) {
        return scheduleRefinerService.generateAndRefineScheduleForApi(prompt);
    }

    private Map<String, String> parseDescriptionMap(String gptResponse) {
        Map<String, String> map = new java.util.LinkedHashMap<>();
        try (java.util.Scanner scanner = new java.util.Scanner(gptResponse)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("-")) {
                    line = line.substring(1).trim();
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        map.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        }
        return map;
    }
    public ScheduleDetailFullResponse updatePlace(PlaceUpdateRequest request) throws Exception {
        // 1. KakaoMap으로 새 장소 검색
        KakaoPlaceDto kakaoPlace = kakaoMapClient.searchPlaceByKeyword(request.getNewPlaceName());

        // 2. GPT로 한줄 설명 생성
        String descPrompt = descriptionPromptBuilder.build(List.of(kakaoPlace.getPlaceName()));
        String descResponse = openAiClient.callGpt(descPrompt);
        String description = parseDescriptionMap(descResponse).get(kakaoPlace.getPlaceName());

        // 3. 장소 객체 생성
        PlaceDetailDto updatedPlace = new PlaceDetailDto();
        updatedPlace.setName(request.getNewPlaceName());
        updatedPlace.setAddress(kakaoPlace.getAddress());
        updatedPlace.setLat(kakaoPlace.getLatitude());
        updatedPlace.setLng(kakaoPlace.getLongitude());
        updatedPlace.setType("관광지"); // 상황 따라 수정
        updatedPlace.setDescription(description);

        // 4. 예산 및 이동 시간 추정
        String costPrompt = costAndTimePromptBuilder.build(List.of(updatedPlace));
        String costResponse = openAiClient.callGpt(costPrompt);

        return new ScheduleDetailFullResponse(
                costAndTimePromptBuilder.parseGptResponse(costResponse, List.of(updatedPlace))
        );
    }


    public List<DaySummaryResponse> createDaySummaries(Map<String, List<PlaceDetailDto>> groupedByDate) {
        List<DaySummaryResponse> summaries = new java.util.ArrayList<>();

        for (Map.Entry<String, List<PlaceDetailDto>> entry : groupedByDate.entrySet()) {
            String date = entry.getKey();
            List<PlaceDetailDto> places = entry.getValue();

            int totalEstimatedCost = places.stream()
                    .map(PlaceDetailDto::getEstimatedCost)
                    .filter(cost -> cost != null)
                    .mapToInt(Integer::intValue)
                    .sum();

            DaySummaryResponse summary = new DaySummaryResponse(date, places, totalEstimatedCost);
            summaries.add(summary);
        }

        return summaries;
    }

    @Transactional
    public SaveScheduleResponse saveSchedule(SaveScheduleRequest request) {

        // 1. Schedule 저장
        Schedule schedule = Schedule.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        scheduleRepository.save(schedule);

        // 2. Day 저장
        for (SaveScheduleRequest.DayRequest dayRequest : request.getDays()) {
            Day day = Day.builder()
                    .schedule(schedule) // ✅ Schedule 엔티티 자체를 참조
                    .dayNumber(dayRequest.getDayNumber())
                    .build();

            dayRepository.save(day);

            // 3. Place 저장
            for (SaveScheduleRequest.PlaceRequest placeRequest : dayRequest.getPlaces()) {
                Place place = Place.builder()
                        .day(day)
                        .name(placeRequest.getName())
                        .type(placeRequest.getType())
                        .address(placeRequest.getAddress())
                        .lat(placeRequest.getLat())
                        .lng(placeRequest.getLng())
                        .description(placeRequest.getDescription())
                        .estimatedCost(placeRequest.getEstimatedCost())
                        .gptOriginalName(placeRequest.getGptOriginalName())
                        .placeOrder(placeRequest.getPlaceOrder())
                        .fromPrevious(Optional.ofNullable(placeRequest.getFromPrevious())
                        .map(dto -> new FromPrevious(dto.getWalk(), dto.getPublicTransport(), dto.getCar()))
                        .orElse(null))
                        .build();
                placeRepository.save(place);
            }
        }

        return new SaveScheduleResponse(schedule.getId());
    }

    public List<PlaceDetailDto> getPlacesFromDatabase(Long scheduleId) {
        List<Place> places = scheduleRepository.findAllPlacesByScheduleId(scheduleId);
        return places.stream()
                .map(place -> PlaceDetailDto.builder()
                        .name(place.getName())
                        .type(place.getType())
                        .lat(place.getLat())
                        .lng(place.getLng())
                        .address(place.getAddress())
                        .estimatedCost(place.getEstimatedCost())
                        .description(place.getDescription())
                        .gptOriginalName(place.getGptOriginalName())
                        .fromPrevious(FromPreviousDto.fromEntity(place.getFromPrevious()))
                        .build())
                .toList();
    }

    public List<ScheduleSimpleResponse> getSimpleScheduleList(Long userId) {
        List<Schedule> schedules = scheduleRepository.findByUserId(userId);

        return schedules.stream()
                .map(schedule -> {
                    int ddayValue = calculateDDay(schedule.getStartDate());
                    String ddayString = formatDday(ddayValue);
                    return new ScheduleSimpleResponse(
                            schedule.getId(),
                            schedule.getTitle(),
                            schedule.getStartDate(),
                            schedule.getEndDate(),
                            ddayString
                    );
                })
                .toList();
    }

    private int calculateDDay(LocalDate startDate) {
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), startDate);
    }

    private String formatDday(int d) {
        if (d == 0) return "D-Day";
        return (d > 0) ? "D-" + d : "D+" + Math.abs(d);
    }




    private int calculateTotalEstimatedCost(List<PlaceDetailDto> places) {
        return places.stream()
                .map(PlaceDetailDto::getEstimatedCost)
                .filter(cost -> cost != null)
                .mapToInt(Integer::intValue)
                .sum();
    }




}
