package com.example.capstone.chatbot.service;

import com.example.capstone.chatbot.dto.request.ChatBotRecreateReqDto;
import com.example.capstone.chatbot.dto.response.FestivalResDto;
import com.example.capstone.chatbot.dto.response.FoodResDto;
import com.example.capstone.chatbot.dto.response.HotelResDto;
import com.example.capstone.chatbot.dto.response.SpotResDto;
import com.example.capstone.chatbot.entity.ChatCategory;
import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import com.example.capstone.plan.service.KakaoMapClient;
import com.example.capstone.plan.service.OpenAiClient;
import com.example.capstone.util.chatbot.recreate.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DestinationChatRecreateService {

    private final OpenAiClient openAiClient;
    private final ChatBotParseService parseService;
    private final KakaoMapClient kakaoMapClient;
    private final TourApiClient tourApiClient;

    private final FoodRecreatePromptBuilder foodRecreatePromptBuilder;
    private final HotelRecreatePromptBuilder hotelRecreatePromptBuilder;
    private final FestivalRecreatePromptBuilder festivalRecreatePromptBuilder;


    public List<FoodResDto> recreateFood(ChatBotRecreateReqDto req) {
        List<KakaoPlaceDto> topPlaces = kakaoMapClient
                .searchTopPlacesByCityAndCategory(req.getCity(), "FD6", 10); // 음식점

        return topPlaces.stream()
                .filter(place -> !req.getExcludedNames().contains(place.getPlaceName()))
                .limit(3)
                .map(place -> {
                    try {
                        String prompt = foodRecreatePromptBuilder.build(place);
                        String response = openAiClient.callGpt(prompt);
                        return ((List<FoodResDto>) parseService.parseResponse(ChatCategory.FOOD, "[" + response + "]")).get(0);
                    } catch (Exception e) {
                        throw new RuntimeException("음식점 GPT 파싱 실패: " + place.getPlaceName(), e);
                    }
                })
                .toList();
    }

    public List<HotelResDto> recreateHotel(ChatBotRecreateReqDto req) {
        List<KakaoPlaceDto> topPlaces = kakaoMapClient
                .searchTopPlacesByCityAndCategory(req.getCity(), "AD5", 10); // 숙소

        return topPlaces.stream()
                .filter(place -> !req.getExcludedNames().contains(place.getPlaceName()))
                .limit(3)
                .map(place -> {
                    try {
                        String prompt = hotelRecreatePromptBuilder.build(place);
                        String response = openAiClient.callGpt(prompt);
                        return ((List<HotelResDto>) parseService.parseResponse(ChatCategory.HOTEL, "[" + response + "]")).get(0);
                    } catch (Exception e) {
                        throw new RuntimeException("숙소 GPT 파싱 실패: " + place.getPlaceName(), e);
                    }
                })
                .toList();
    }
    public List<FestivalResDto> recreateFestival(ChatBotRecreateReqDto req) {
        try {
            // 1. 필터링된 축제 목록 가져오기
            JsonNode filteredJson = tourApiClient.getFestivalListByCityExcluding(
                    req.getCity(),
                    LocalDate.now(),
                    req.getExcludedNames()
            );

            // 2. 프롬프트 생성 및 GPT 호출
            String prompt = festivalRecreatePromptBuilder.build(filteredJson);
            String gptResponse = openAiClient.callGpt(prompt);

            // 3. 응답 파싱
            return (List<FestivalResDto>) parseService.parseResponse(ChatCategory.FESTIVAL, gptResponse);
        } catch (Exception e) {
            throw new RuntimeException("축제 재조회 GPT 파싱 실패", e);
        }
    }
}
