// ✅ 최종 리팩토링된 ScheduleService.java
package com.example.capstone.plan.service;

import com.example.capstone.plan.dto.common.FromPreviousDto;
import com.example.capstone.plan.dto.common.PlaceDetailDto;
import com.example.capstone.plan.dto.request.ScheduleSaveReqDto;
import com.example.capstone.plan.dto.request.ScheduleCreateReqDto;
import com.example.capstone.plan.dto.response.ScheduleSaveResDto;
import com.example.capstone.plan.dto.response.FullScheduleResDto;
import com.example.capstone.plan.dto.response.FullScheduleResDto.PlaceResponse;
import com.example.capstone.plan.dto.response.FullScheduleResDto.DailyScheduleBlock;
import com.example.capstone.plan.dto.response.SimpleScheduleResDto;
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
import java.util.stream.Collectors;

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

    public FullScheduleResDto generateFullSchedule(ScheduleCreateReqDto request) throws Exception {
        String schedulePrompt = structurePromptBuilder.build(request);
        String gptResponse = openAiClient.callGpt(schedulePrompt);

        List<PlaceDetailDto> places = scheduleRefinerService.getRefinedPlacesFromPrompt(gptResponse);

        List<String> placeNames = places.stream().map(PlaceDetailDto::getName).toList();
        String descPrompt = descriptionPromptBuilder.build(placeNames);
        String descResponse = openAiClient.callGpt(descPrompt);
        var descriptionMap = parseDescriptionMap(descResponse);
        for (PlaceDetailDto place : places) {
            String desc = descriptionMap.get(place.getName());
            if (desc != null) place.setDescription(desc);
        }

        String costPrompt = costAndTimePromptBuilder.build(places);
        String costResponse = openAiClient.callGpt(costPrompt);
        List<PlaceResponse> responses = costAndTimePromptBuilder.parseGptResponse(costResponse, places);
        List<PlaceDetailDto> enrichedPlaces = responses.stream().map(PlaceResponse::toDto).toList();

        Map<String, List<PlaceDetailDto>> groupedByDate = enrichedPlaces.stream()
                .collect(Collectors.groupingBy(PlaceDetailDto::getDate));

        List<String> sortedDates = groupedByDate.keySet().stream().sorted().toList();
        List<FullScheduleResDto.DailyScheduleBlock> blocks = new ArrayList<>();

        for (int i = 0; i < sortedDates.size(); i++) {
            String date = sortedDates.get(i);
            String day = (i + 1) + "일차";
            List<PlaceDetailDto> dayPlaces = groupedByDate.get(date);

            int total = dayPlaces.stream().map(PlaceDetailDto::getEstimatedCost).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
            List<PlaceResponse> placeResponses = dayPlaces.stream().map(PlaceResponse::from).toList();

            blocks.add(new FullScheduleResDto.DailyScheduleBlock(day, date, total, placeResponses));
        }

        String destination = request.getDestination().name();
        long nights = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        String title = destination + " " + nights + "박 " + (nights + 1) + "일 여행";

        return new FullScheduleResDto(title, request.getStartDate(), request.getEndDate(), blocks);
    }

    private Map<String, String> parseDescriptionMap(String gptResponse) {
        Map<String, String> map = new LinkedHashMap<>();
        try (Scanner scanner = new Scanner(gptResponse)) {
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

    @Transactional
    public ScheduleSaveResDto saveSchedule(ScheduleSaveReqDto request) {
        Schedule schedule = Schedule.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        scheduleRepository.save(schedule);

        for (ScheduleSaveReqDto.DayRequest dayRequest : request.getDays()) {
            Day day = Day.builder().schedule(schedule).build();
            dayRepository.save(day);

            List<ScheduleSaveReqDto.PlaceRequest> placeRequests = dayRequest.getPlaces();
            for (int i = 0; i < placeRequests.size(); i++) {
                ScheduleSaveReqDto.PlaceRequest placeRequest = placeRequests.get(i);
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
                        .placeOrder(i)
                        .fromPrevious(Optional.ofNullable(placeRequest.getFromPrevious())
                                .map(dto -> new FromPrevious(dto.getWalk(), dto.getPublicTransport(), dto.getCar()))
                                .orElse(null))
                        .build();
                placeRepository.save(place);
            }
        }

        return new ScheduleSaveResDto(schedule.getId());
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

    public FullScheduleResDto convertToBlockStructure(List<PlaceDetailDto> places, Schedule schedule) {
        Map<String, List<PlaceDetailDto>> groupedByDate = places.stream()
                .collect(Collectors.groupingBy(PlaceDetailDto::getDate));

        List<String> sortedDates = groupedByDate.keySet().stream().sorted().toList();
        List<DailyScheduleBlock> blocks = new ArrayList<>();

        for (int i = 0; i < sortedDates.size(); i++) {
            String date = sortedDates.get(i);
            String day = (i + 1) + "일차";
            List<PlaceDetailDto> dayPlaces = groupedByDate.get(date);

            int total = dayPlaces.stream()
                    .map(PlaceDetailDto::getEstimatedCost)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

            List<PlaceResponse> placeResponses = dayPlaces.stream()
                    .map(PlaceResponse::from)
                    .toList();

            blocks.add(new DailyScheduleBlock(day, date, total, placeResponses));
        }

        return new FullScheduleResDto(
                schedule.getTitle(),
                schedule.getStartDate(),
                schedule.getEndDate(),
                blocks
        );
    }
    public Schedule getScheduleById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException(" 해당 ID의 일정이 없습니다: " + scheduleId));
    }

    public List<SimpleScheduleResDto> getSimpleScheduleList(Long userId) {
        List<Schedule> schedules = scheduleRepository.findByUserId(userId);
        return schedules.stream()
                .map(schedule -> new SimpleScheduleResDto(
                        schedule.getId(),
                        schedule.getTitle(),
                        schedule.getStartDate(),
                        schedule.getEndDate(),
                        formatDday(calculateDDay(schedule.getStartDate()))
                )).toList();
    }

    private int calculateDDay(LocalDate startDate) {
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), startDate);
    }

    private String formatDday(int d) {
        if (d == 0) return "D-Day";
        return (d > 0) ? "D-" + d : "D+" + Math.abs(d);
    }
}
