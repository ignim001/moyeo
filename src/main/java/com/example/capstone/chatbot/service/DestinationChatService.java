package com.example.capstone.chatbot.service;

import com.example.capstone.chatbot.dto.response.*;
import com.example.capstone.chatbot.entity.ChatCategory;
import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import com.example.capstone.plan.service.KakaoMapClient;
import com.example.capstone.util.chatbot.FestivalPromptBuilder;
import com.example.capstone.util.chatbot.FoodPromptBuilder;
import com.example.capstone.util.chatbot.HotelPromptBuilder;
import com.example.capstone.plan.entity.City;
import com.example.capstone.plan.service.OpenAiClient;
import com.example.capstone.util.chatbot.SpotPromptBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.capstone.chatbot.service.ChatBotParseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class DestinationChatService {

    private final OpenWeatherClient openWeatherClient;
    private final KakaoMapClient kakaoMapClient;
    private final FoodPromptBuilder foodPromptBuilder;
    private final HotelPromptBuilder hotelPromptBuilder;
    private final FestivalPromptBuilder festivalPromptBuilder;
    private final SpotPromptBuilder spotPromptBuilder;
    private final OpenAiClient openAiClient;
    private final TourApiClient tourApiClient;
    private final ChatBotParseService parseService;
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
                city.getDisplayName()
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
                        System.out.println("🧠 GPT 응답 원문:\n" + response);
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
    public List<FestivalResDto> getFestivalList(City city) {
        JsonNode json = tourApiClient.getFestivalListByCity(city, LocalDate.now());
        List<JsonNode> rawFestivals = extractFestivalItems(json);

        List<FestivalResDto> result = new ArrayList<>();
        for (JsonNode item : rawFestivals) {
            String prompt = festivalPromptBuilder.buildSingle(item);
            String gptResponse = openAiClient.callGpt(prompt);

            try {
                FestivalResDto dto = (FestivalResDto) parseService.parseResponse(ChatCategory.FESTIVAL, gptResponse);
                result.add(dto);
            } catch (Exception e) {
                System.err.println("❌ GPT 축제 파싱 실패: " + e.getMessage());
            }

            if (result.size() >= 3) break;
        }

        return result;
    }
    private List<JsonNode> extractFestivalItems(JsonNode responseJson) {
        JsonNode items = responseJson.at("/response/body/items/item");
        if (items.isMissingNode()) return List.of();
        if (items.isArray()) {
            return StreamSupport.stream(items.spliterator(), false).collect(Collectors.toList());
        } else {
            return List.of(items);
        }
    }



    public List<SpotResDto> getSpot(City city) throws Exception {
        List<SpotResDto> results = new ArrayList<>();
        List<String> excludedNames = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            String prompt = spotPromptBuilder.build(i, city, null, null);
            String gptResponse = openAiClient.callGpt(prompt);

            SpotResDto dto = (SpotResDto) parseService.parseResponse(ChatCategory.SPOT, gptResponse);
            if (dto == null || excludedNames.contains(dto.getName())) {
                i--; // 실패하거나 중복이면 다시 시도
                continue;
            }

            results.add(dto);
            excludedNames.add(dto.getName());
        }

        return results;
    }








}
