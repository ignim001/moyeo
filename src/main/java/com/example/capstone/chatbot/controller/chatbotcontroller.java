package com.example.capstone.chatbot.controller;

import com.example.capstone.chatbot.dto.ChatBotReq;
import com.example.capstone.chatbot.dto.ChatBotGpsReq;
import com.example.capstone.chatbot.entity.ChatCategory;
import com.example.capstone.chatbot.service.chatbotservice;
import com.example.capstone.plan.entity.City;
import com.example.capstone.plan.service.OpenAiClient;
import com.example.capstone.util.chatbot.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.capstone.chatbot.entity.ChatCategory.*;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
public class chatbotcontroller {

    // 일반 정보 프롬프트 빌더
    private final SpotPromptBuilder spotPromptBuilder;
    private final FoodPromptBuilder foodPromptBuilder;
    private final HotelPromptBuilder hotelPromptBuilder;
    private final FestivalPromptBuilder festivalPromptBuilder;
    private final WeatherPromptBuilder weatherPromptBuilder;
    private final GPSSpotPromptBuilder gpsSpotPromptBuilder;
    private final GPSFoodPromptBuilder gpsFoodPromptBuilder;
    private final GPSHotelPromptBuilder gpsHotelPromptBuilder;
    private final GPSFestivalPromptBuilder gpsFestivalPromptBuilder;
    private final GPSWeatherPromptBuilder gpsWeatherPromptBuilder;

    // GPT 호출 클라이언트
    private final OpenAiClient openAiClient;
    private final chatbotservice chatbotservice;

    /**
     * 목적지 기반 질문 처리
     */
    @PostMapping("/info")
    public ResponseEntity<?> getInfoByDestination(@RequestBody ChatBotReq request) throws Exception {
        City city = request.getCity();
        ChatCategory category = request.getCategory();

        String prompt = switch (category) {
            case SPOT -> spotPromptBuilder.build(city);
            case FOOD -> foodPromptBuilder.build(city);
            case HOTEL -> hotelPromptBuilder.build(city);
            case FESTIVAL -> festivalPromptBuilder.build(city);
            case WEATHER -> weatherPromptBuilder.build(city);
        };

        String gptResponse = openAiClient.callGpt(prompt);
        return ResponseEntity.ok(gptResponse);
    }

    @PostMapping("/gps")
    public ResponseEntity<?> getInfoByLocation(@RequestBody ChatBotGpsReq request) throws Exception {
        double lat = request.getLatitude();
        double lng = request.getLongitude();
        ChatCategory category = request.getCategory();


        String prompt = switch (category) {
            case SPOT -> gpsSpotPromptBuilder.build(lat, lng);
            case FOOD -> gpsFoodPromptBuilder.build(lat, lng);
            case HOTEL -> gpsHotelPromptBuilder.build(lat, lng);
            case FESTIVAL -> gpsFestivalPromptBuilder.build(lat, lng);
            case WEATHER -> gpsWeatherPromptBuilder.build(lat, lng);
        };

        String gptResponse = openAiClient.callGpt(prompt);
        Object parsed = chatbotservice.parseResponse(category, gptResponse);
        return ResponseEntity.ok(parsed);
    }




}
