package com.example.capstone.chatbot.service;

import com.example.capstone.chatbot.dto.response.FestivalResDto;
import com.example.capstone.chatbot.dto.response.FoodResDto;
import com.example.capstone.chatbot.dto.response.HotelResDto;
import com.example.capstone.chatbot.dto.response.WeatherResDto;
import com.example.capstone.chatbot.entity.ChatCategory;
import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import com.example.capstone.plan.entity.City;
import com.example.capstone.plan.service.KakaoMapClient;
import com.example.capstone.plan.service.OpenAiClient;
import com.example.capstone.chatbot.service.TourApiClient;
import com.example.capstone.util.chatbot.FestivalPromptBuilder;
import com.example.capstone.util.chatbot.FoodPromptBuilder;
import com.example.capstone.util.chatbot.HotelPromptBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GpsChatService {

    private final OpenWeatherClient openWeatherClient;
    private final KakaoMapClient kakaoMapClient;
    private final FoodPromptBuilder foodPromptBuilder;
    private final HotelPromptBuilder hotelPromptBuilder;
    private final FestivalPromptBuilder festivalPromptBuilder;
    private final OpenAiClient openAiClient;
    private final TourApiClient tourApiClient;
    private final ChatBotParseService parseService;
    private final ObjectMapper objectMapper;

    public WeatherResDto getWeather(double lat, double lng) {
        City city = kakaoMapClient.getCityFromLatLng(lat, lng);  // 시 이름 추출
        String regionName = city.getDisplayName();               // 예: "청주시"

        KakaoPlaceDto place = kakaoMapClient.searchPlace(regionName); // 시 이름 → 위경도
        if (place == null) {
            throw new IllegalArgumentException("해당 도시의 위치 정보를 찾을 수 없습니다.");
        }

        return openWeatherClient.getWeather(
                place.getLatitude(),
                place.getLongitude(),
                regionName
        );
    }
    public List<FoodResDto> getFoodList(double lat, double lng) {
        List<KakaoPlaceDto> places = kakaoMapClient.searchPlacesByCategory(lat, lng, "FD6"); // 음식점

        return places.stream()
                .limit(3)
                .map(place -> {
                    try {
                        String prompt = foodPromptBuilder.build(place);
                        String response = openAiClient.callGpt(prompt);

                        return objectMapper.readValue(response, FoodResDto.class);
                    } catch (Exception e) {
                        throw new RuntimeException("GPS 음식점 GPT 처리 실패: " + place.getPlaceName(), e);
                    }
                })
                .collect(Collectors.toList());
    }


    public List<HotelResDto> getHotelList(double lat, double lng) {
        List<KakaoPlaceDto> places = kakaoMapClient.searchPlacesByCategory(lat, lng, "AD5"); // 숙소

        return places.stream()
                .limit(3)
                .map(place -> {
                    try {
                        String prompt = hotelPromptBuilder.build(place);
                        String response = openAiClient.callGpt(prompt);

                        return objectMapper.readValue(response, HotelResDto.class);
                    } catch (Exception e) {
                        throw new RuntimeException("GPS 숙소 GPT 처리 실패: " + place.getPlaceName(), e);
                    }
                })
                .collect(Collectors.toList());
    }
    public List<FestivalResDto> getFestivalList(double lat, double lng) {
        JsonNode json = tourApiClient.getFestivalList(lat, lng, LocalDate.now());
        String prompt = festivalPromptBuilder.build(json);
        String gptResponse = openAiClient.callGpt(prompt);
        try {
            List<FestivalResDto> fullList = (List<FestivalResDto>) parseService.parseResponse(ChatCategory.FESTIVAL, gptResponse);
            return fullList.stream()
                    .limit(3)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("GPT 축제 파싱 실패", e);
        }
    }



}
