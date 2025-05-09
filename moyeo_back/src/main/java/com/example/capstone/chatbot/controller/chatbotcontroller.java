package com.example.capstone.chatbot.controller;

import com.example.capstone.chatbot.dto.ChatBotReq;
import com.example.capstone.chatbot.dto.ChatBotGpsReq;
import com.example.capstone.plan.service.OpenAiClient;
import com.example.capstone.util.chatbot.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
public class chatbotcontroller {

    // 일반 정보 프롬프트 빌더
    private final InfoPromptBuilder tourInfoPromptBuilder;
    private final FoodPromptBuilder foodInfoPromptBuilder;
    private final HotelPromptBuilder hotelInfoPromptBuilder;
    private final FestivalPromptBuilder festivalInfoPromptBuilder;
    private final WeatherPromptBuilder weatherInfoPromptBuilder;

    // GPS 기반 프롬프트 빌더
    private final GPSInfoPromptBuilder gpsInfoPromptBuilder;
    private final GPSFoodPromptBuilder gpsFoodPromptBuilder;
    private final GPSHotelPromptBuilder gpsHotelPromptBuilder;
    private final GPSFestivalPromptBuilder gpsFestivalPromptBuilder;
    private final GPSWeatherPromptBuilder gpsWeatherPromptBuilder;

    // GPT 호출 클라이언트
    private final OpenAiClient openAiClient;

    /**
     * 목적지 기반 질문 처리
     */
    @PostMapping("/ask")
    public ResponseEntity<String> ask(@RequestBody ChatBotReq request) throws Exception {
        String prompt = buildPromptByCategory(request.getCategory(), request.getUserInput());
        String gptResponse = openAiClient.callGpt(prompt);
        return ResponseEntity.ok(gptResponse);
    }

    private String buildPromptByCategory(String category, String userInput) {
        return switch (category.toLowerCase()) {
            case "관광지" -> tourInfoPromptBuilder.build(userInput);
            case "맛집", "카페" -> foodInfoPromptBuilder.build(userInput);
            case "숙소" -> hotelInfoPromptBuilder.build(userInput);
            case "축제", "이벤트" -> festivalInfoPromptBuilder.build(userInput);
            case "날씨" -> weatherInfoPromptBuilder.build(userInput);
            default -> "[시스템 오류] 유효하지 않은 카테고리입니다.";
        };
    }

    /**
     * GPS 기반 질문 처리
     */
    @PostMapping("/ask/gps")
    public ResponseEntity<String> askWithGps(@RequestBody ChatBotGpsReq request) throws Exception {
        String prompt = buildPromptByCategory(
                request.getCategory(),
                request.getUserInput(),
                request.getLatitude(),
                request.getLongitude()
        );
        String gptResponse = openAiClient.callGpt(prompt);
        return ResponseEntity.ok(gptResponse);
    }

    private String buildPromptByCategory(String category, String userInput, double lat, double lng) {
        return switch (category.toLowerCase()) {
            case "관광지" -> gpsInfoPromptBuilder.build(userInput, lat, lng);
            case "맛집", "카페" -> gpsFoodPromptBuilder.build(userInput, lat, lng);
            case "숙소" -> gpsHotelPromptBuilder.build(userInput, lat, lng);
            case "축제", "이벤트" -> gpsFestivalPromptBuilder.build(userInput, lat, lng);
            case "날씨" -> gpsWeatherPromptBuilder.build(userInput, lat, lng);
            default -> "[시스템 오류] 유효하지 않은 카테고리입니다.";
        };
    }
}
