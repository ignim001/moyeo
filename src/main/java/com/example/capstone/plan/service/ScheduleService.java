package com.example.capstone.plan.service;

import com.example.capstone.plan.dto.common.FromPreviousDto;
import com.example.capstone.plan.dto.common.PlaceDetailDto;
import com.example.capstone.plan.dto.request.ScheduleRecreateReqDto;
import com.example.capstone.plan.dto.request.ScheduleSaveReqDto;
import com.example.capstone.plan.dto.request.ScheduleCreateReqDto;
import com.example.capstone.plan.dto.response.ScheduleSaveResDto;
import com.example.capstone.plan.dto.response.FullScheduleResDto;
import com.example.capstone.plan.dto.response.FullScheduleResDto.PlaceResponse;
import com.example.capstone.plan.dto.response.FullScheduleResDto.DailyScheduleBlock;
import com.example.capstone.plan.dto.response.SimpleScheduleResDto;
import com.example.capstone.plan.entity.TravelDay;
import com.example.capstone.plan.entity.FromPrevious;
import com.example.capstone.plan.entity.TravelPlace;
import com.example.capstone.plan.entity.TravelSchedule;
import com.example.capstone.plan.repository.DayRepository;
import com.example.capstone.plan.repository.PlaceRepository;
import com.example.capstone.plan.repository.ScheduleRepository;
import com.example.capstone.user.entity.UserEntity;
import com.example.capstone.user.repository.UserRepository;
import com.example.capstone.util.gpt.GptCostAndTimePromptBuilder;
import com.example.capstone.util.gpt.GptPlaceDescriptionPromptBuilder;
import com.example.capstone.util.gpt.GptRecreatePromptBuilder;
import com.example.capstone.util.gpt.GptScheduleStructurePromptBuilder;
import com.example.capstone.util.oauth2.dto.CustomOAuth2User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
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
    private final GptRecreatePromptBuilder gptRecreatePromptBuilder;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;
    private final KakaoMapClient kakaoMapClient;
    private final ScheduleRepository scheduleRepository;
    private final DayRepository dayRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

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
        List<FullScheduleResDto.PlaceResponse> finalPlaces =
                scheduleRefinerService.parseGptResponse(costResponse, places);
        List<PlaceDetailDto> enrichedPlaces = finalPlaces.stream().map(PlaceResponse::toDto).toList();

        Map<Integer, List<PlaceDetailDto>> groupedByDayIndex = new LinkedHashMap<>();
        int days = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        for (int i = 0; i < days; i++) groupedByDayIndex.put(i, new ArrayList<>());
        for (int i = 0; i < enrichedPlaces.size(); i++) {
            int dayIndex = i / 7;
            if (dayIndex < days) groupedByDayIndex.get(dayIndex).add(enrichedPlaces.get(i));
        }

        List<DailyScheduleBlock> blocks = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = request.getStartDate().plusDays(i);
            String day = (i + 1) + "일차";
            List<PlaceDetailDto> dayPlaces = groupedByDayIndex.get(i);
            int total = dayPlaces.stream().map(PlaceDetailDto::getEstimatedCost).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
            List<PlaceResponse> placeResponses = dayPlaces.stream().map(PlaceResponse::from).toList();
            blocks.add(new DailyScheduleBlock(day, date.toString(), total, placeResponses));
        }

        String destination = request.getDestination().getDisplayName();
        long nights = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        String title = destination + " " + nights + "박 " + (nights + 1) + "일 여행";

        return new FullScheduleResDto(title, request.getStartDate(), request.getEndDate(), blocks);
    }


    private Map<String, String> parseDescriptionMap(String gptResponse) {
        try {
            return objectMapper.readValue(gptResponse, new TypeReference<>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }




    @Transactional
    public ScheduleSaveResDto saveSchedule(ScheduleSaveReqDto request, CustomOAuth2User userDetails) {
        String providerId = userDetails.getProviderId();
        UserEntity user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        TravelSchedule travelSchedule = TravelSchedule.builder()
                .user(user)
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        scheduleRepository.save(travelSchedule);

        LocalDate currentDate = request.getStartDate();
        for (int i = 0; i < request.getDays().size(); i++) {
            ScheduleSaveReqDto.DayRequest dayRequest = request.getDays().get(i);
            String dayLabel = (i + 1) + "일차";

            TravelDay travelDay = TravelDay.builder()
                    .travelSchedule(travelSchedule)
                    .dayNumber(i + 1)
                    .date(currentDate.toString())
                    .day(dayLabel)
                    .build();
            dayRepository.save(travelDay);

            List<ScheduleSaveReqDto.PlaceRequest> placeRequests = dayRequest.getPlaces();
            for (int j = 0; j < placeRequests.size(); j++) {
                ScheduleSaveReqDto.PlaceRequest placeRequest = placeRequests.get(j);
                TravelPlace travelPlace = TravelPlace.builder()
                        .travelDay(travelDay)
                        .name(placeRequest.getName())
                        .type(placeRequest.getType())
                        .address(placeRequest.getAddress())
                        .lat(placeRequest.getLat())
                        .lng(placeRequest.getLng())
                        .description(placeRequest.getDescription())
                        .estimatedCost(placeRequest.getEstimatedCost())
                        .gptOriginalName(placeRequest.getGptOriginalName())
                        .placeOrder(j)
                        .fromPrevious(Optional.ofNullable(placeRequest.getFromPrevious())
                                .map(dto -> new FromPrevious(dto.getWalk(), dto.getPublicTransport(), dto.getCar()))
                                .orElse(null))
                        .build();
                placeRepository.save(travelPlace);
            }
            currentDate = currentDate.plusDays(1);
        }

        return new ScheduleSaveResDto(travelSchedule.getId());
    }

    public List<SimpleScheduleResDto> getSimpleScheduleList(CustomOAuth2User userDetails) {
        String providerId = userDetails.getProviderId();
        UserEntity user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<TravelSchedule> travelSchedules = scheduleRepository.findByUserId(user.getId());
        return travelSchedules.stream()
                .map(schedule -> new SimpleScheduleResDto(
                        schedule.getId(),
                        schedule.getTitle(),
                        schedule.getStartDate(),
                        schedule.getEndDate(),
                        formatDday(calculateDDay(schedule.getStartDate()))
                )).toList();
    }

    public FullScheduleResDto getFullSchedule(Long scheduleId, CustomOAuth2User userDetails) {
        String providerId = userDetails.getProviderId();
        UserEntity user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        TravelSchedule schedule = scheduleRepository.findByIdAndUserId(scheduleId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("접근 권한이 없습니다."));

        List<PlaceDetailDto> places = getPlacesFromDatabase(scheduleId);
        return convertToBlockStructure(places, schedule);
    }


    public List<PlaceDetailDto> getPlacesFromDatabase(Long scheduleId) {
        List<TravelPlace> travelPlaces = scheduleRepository.findAllPlacesByScheduleId(scheduleId);
        return travelPlaces.stream()
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

    public FullScheduleResDto convertToBlockStructure(List<PlaceDetailDto> places, TravelSchedule travelSchedule) {
        int days = (int) ChronoUnit.DAYS.between(travelSchedule.getStartDate(), travelSchedule.getEndDate()) + 1;
        Map<Integer, List<PlaceDetailDto>> groupedByDayIndex = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) groupedByDayIndex.put(i, new ArrayList<>());
        for (int i = 0; i < places.size(); i++) {
            int dayIndex = i / 7;
            if (dayIndex < days) groupedByDayIndex.get(dayIndex).add(places.get(i));
        }

        List<DailyScheduleBlock> blocks = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = travelSchedule.getStartDate().plusDays(i);
            String day = (i + 1) + "일차";
            List<PlaceDetailDto> dayPlaces = groupedByDayIndex.get(i);

            int total = dayPlaces.stream()
                    .map(PlaceDetailDto::getEstimatedCost)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

            List<PlaceResponse> placeResponses = dayPlaces.stream()
                    .map(PlaceResponse::from)
                    .toList();

            blocks.add(new DailyScheduleBlock(day, date.toString(), total, placeResponses));
        }

        return new FullScheduleResDto(
                travelSchedule.getTitle(),
                travelSchedule.getStartDate(),
                travelSchedule.getEndDate(),
                blocks
        );
    }

    public FullScheduleResDto recreateSchedule(ScheduleRecreateReqDto regenerateRequest) throws Exception {
        ScheduleCreateReqDto request = regenerateRequest.getRequest();
        List<String> excludePlaceNames = regenerateRequest.getExcludedNames();

        String prompt = gptRecreatePromptBuilder.build(request, excludePlaceNames);
        String gptResponse = openAiClient.callGpt(prompt);

        List<PlaceDetailDto> refinedPlaces = scheduleRefinerService.getRefinedPlacesFromPrompt(gptResponse);

        List<String> placeNames = refinedPlaces.stream().map(PlaceDetailDto::getName).toList();
        String descPrompt = descriptionPromptBuilder.build(placeNames);
        String descResponse = openAiClient.callGpt(descPrompt);
        Map<String, String> descriptionMap = parseDescriptionMap(descResponse);
        for (PlaceDetailDto place : refinedPlaces) {
            place.setDescription(descriptionMap.getOrDefault(place.getName(), ""));
        }

        String costPrompt = costAndTimePromptBuilder.build(refinedPlaces);
        String costResponse = openAiClient.callGpt(costPrompt);
        List<FullScheduleResDto.PlaceResponse> finalPlaces =
                scheduleRefinerService.parseGptResponse(costResponse, refinedPlaces);

        long nights = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        long days = nights + 1;

        List<FullScheduleResDto.DailyScheduleBlock> blocks = new ArrayList<>();
        int placesPerDay = finalPlaces.size() / (int) days;
        int index = 0;

        for (int i = 0; i < days; i++) {
            String day = (i + 1) + "일차";
            String date = request.getStartDate().plusDays(i).toString();

            int remaining = finalPlaces.size() - index;
            int currentDayCount = i == days - 1 ? remaining : placesPerDay;

            List<FullScheduleResDto.PlaceResponse> dailyPlaces =
                    finalPlaces.subList(index, index + currentDayCount);

            int total = dailyPlaces.stream()
                    .mapToInt(PlaceResponse::getEstimatedCost)
                    .sum();

            blocks.add(new FullScheduleResDto.DailyScheduleBlock(day, date, total, dailyPlaces));
            index += currentDayCount;
        }

        String title = request.getDestination().getDisplayName() + " " + nights + "박 " + days + "일 여행";
        return new FullScheduleResDto(title, request.getStartDate(), request.getEndDate(), blocks);
    }

    private int calculateDDay(LocalDate startDate) {
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), startDate);
    }

    private String formatDday(int d) {
        if (d == 0) return "D-Day";
        return (d > 0) ? "D-" + d : "D+" + Math.abs(d);
    }
    public void deleteSchedule(Long scheduleId, CustomOAuth2User userDetails) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        TravelSchedule schedule = scheduleRepository.findByIdAndUserId(scheduleId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Schedule Not Found"));

        scheduleRepository.delete(schedule);
    }
}