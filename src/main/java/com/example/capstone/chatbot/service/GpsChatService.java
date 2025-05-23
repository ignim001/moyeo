package com.example.capstone.chatbot.service;

import com.example.capstone.chatbot.dto.response.FoodResDto;
import com.example.capstone.chatbot.dto.response.HotelResDto;
import com.example.capstone.chatbot.dto.response.WeatherResDto;
import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import com.example.capstone.plan.service.KakaoMapClient;
import com.example.capstone.plan.service.OpenAiClient;
import com.example.capstone.util.chatbot.FoodPromptBuilder;
import com.example.capstone.util.chatbot.HotelPromptBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GpsChatService {

    private final OpenWeatherClient openWeatherClient;
    private final KakaoMapClient kakaoMapClient;
    private final FoodPromptBuilder foodPromptBuilder;
    private final HotelPromptBuilder hotelPromptBuilder;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    public WeatherResDto getWeather(double lat, double lng) {
        String regionName = String.format("현재 위치 (%.2f, %.2f)", lat, lng);
        return openWeatherClient.getWeather(lat, lng, regionName);
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

}
