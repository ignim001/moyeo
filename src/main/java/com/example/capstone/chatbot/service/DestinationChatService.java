package com.example.capstone.chatbot.service;

import com.example.capstone.chatbot.dto.response.WeatherResDto;
import com.example.capstone.chatbot.dto.response.FoodResDto;
import com.example.capstone.chatbot.dto.response.HotelResDto;
import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import com.example.capstone.plan.service.KakaoMapClient;
import com.example.capstone.util.chatbot.FoodPromptBuilder;
import com.example.capstone.util.chatbot.HotelPromptBuilder;
import com.example.capstone.plan.entity.City;
import com.example.capstone.plan.service.OpenAiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DestinationChatService {

    private final OpenWeatherClient openWeatherClient;
    private final KakaoMapClient kakaoMapClient;
    private final FoodPromptBuilder foodPromptBuilder;
    private final HotelPromptBuilder hotelPromptBuilder;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    public WeatherResDto getWeather(City city) {
        String cityName = city.getDisplayName();
        KakaoPlaceDto place = kakaoMapClient.searchPlace(cityName);

        if (place == null) {
            throw new IllegalArgumentException("해당 도시의 위치 정보를 찾을 수 없습니다.");
        }

        return openWeatherClient.getWeather(
                place.getLatitude(),
                place.getLongitude(),
                place.getPlaceName()
        );
    }

    public List<FoodResDto> getFoodList(City city) {
        String keyword = city.getDisplayName();
        String categoryCode = "FD6";

        List<KakaoPlaceDto> places = kakaoMapClient.searchPlacesWithCategory(keyword, categoryCode);

        return places.stream()
                .limit(3)
                .map(place -> {
                    try {
                        String prompt = foodPromptBuilder.build(place);
                        String response = openAiClient.callGpt(prompt);
                        return objectMapper.readValue(response, FoodResDto.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Food GPT 처리 실패: " + place.getPlaceName(), e);
                    }
                })
                .collect(Collectors.toList());
    }


    public List<HotelResDto> getHotelList(City city) {
        String keyword = city.getDisplayName();
        String categoryCode = "AD5";

        List<KakaoPlaceDto> places = kakaoMapClient.searchPlacesWithCategory(keyword, categoryCode);

        return places.stream()
                .limit(3)
                .map(place -> {
                    try {
                        String prompt = hotelPromptBuilder.build(place);
                        String response = openAiClient.callGpt(prompt);


                        return objectMapper.readValue(response, HotelResDto.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Hotel GPT 처리 실패: " + place.getPlaceName(), e);
                    }
                })
                .collect(Collectors.toList());
    }

}
