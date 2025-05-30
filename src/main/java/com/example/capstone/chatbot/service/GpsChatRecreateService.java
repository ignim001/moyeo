package com.example.capstone.chatbot.service;

import com.example.capstone.chatbot.dto.response.FestivalResDto;
import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import com.example.capstone.plan.service.KakaoMapClient;
import com.example.capstone.plan.service.OpenAiClient;
import com.example.capstone.chatbot.dto.response.FoodResDto;
import com.example.capstone.chatbot.dto.response.HotelResDto;
import com.example.capstone.chatbot.entity.ChatCategory;
import com.example.capstone.util.chatbot.recreate.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GpsChatRecreateService {

    private final KakaoMapClient kakaoMapClient;
    private final OpenAiClient openAiClient;
    private final ChatBotParseService parseService;
    private final TourApiClient tourApiClient;

    private final FoodRecreatePromptBuilder foodRecreatePromptBuilder;
    private final HotelRecreatePromptBuilder hotelRecreatePromptBuilder;
    private final FestivalRecreatePromptBuilder festivalRecreatePromptBuilder;

    public List<FoodResDto> recreateFood(double lat, double lng, List<String> excludedNames) {
        List<KakaoPlaceDto> topPlaces = kakaoMapClient.searchTopPlacesByCategory(lat, lng, "FD6", 10); // 음식점

        return topPlaces.stream()
                .filter(place -> !excludedNames.contains(place.getPlaceName()))
                .limit(3)
                .map(place -> {
                    try {
                        String prompt = foodRecreatePromptBuilder.build(place);
                        String response = openAiClient.callGpt(prompt);
                        return ((List<FoodResDto>) parseService.parseResponse(ChatCategory.FOOD, "[" + response + "]")).get(0);
                    } catch (Exception e) {
                        throw new RuntimeException("GPS 음식점 GPT 파싱 실패: " + place.getPlaceName(), e);
                    }
                })
                .toList();
    }

    public List<HotelResDto> recreateHotel(double lat, double lng, List<String> excludedNames) {
        List<KakaoPlaceDto> topPlaces = kakaoMapClient.searchTopPlacesByCategory(lat, lng, "AD5", 10); // 숙소

        return topPlaces.stream()
                .filter(place -> !excludedNames.contains(place.getPlaceName()))
                .limit(3)
                .map(place -> {
                    try {
                        String prompt = hotelRecreatePromptBuilder.build(place);
                        String response = openAiClient.callGpt(prompt);
                        return ((List<HotelResDto>) parseService.parseResponse(ChatCategory.HOTEL, "[" + response + "]")).get(0);
                    } catch (Exception e) {
                        throw new RuntimeException("GPS 숙소 GPT 파싱 실패: " + place.getPlaceName(), e);
                    }
                })
                .toList();
    }
    public List<FestivalResDto> recreateFestival(double lat, double lng, List<String> excludedNames) {
        try {
            JsonNode filteredJson = tourApiClient.getFestivalListByGpsExcluding(
                    lat,
                    lng,
                    LocalDate.now(),
                    excludedNames
            );
            String prompt = festivalRecreatePromptBuilder.build(filteredJson);
            String gptResponse = openAiClient.callGpt(prompt);
            return (List<FestivalResDto>) parseService.parseResponse(ChatCategory.FESTIVAL, gptResponse);
        } catch (Exception e) {
            throw new RuntimeException("GPS 기반 축제 재조회 중 오류 발생", e);
        }
    }

}
