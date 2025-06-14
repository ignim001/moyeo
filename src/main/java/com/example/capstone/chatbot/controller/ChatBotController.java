package com.example.capstone.chatbot.controller;

import com.example.capstone.chatbot.dto.request.*;
import com.example.capstone.chatbot.entity.ChatCategory;
import com.example.capstone.chatbot.service.*;
import com.example.capstone.plan.entity.City;
import com.example.capstone.plan.service.OpenAiClient;
import com.example.capstone.util.chatbot.*;
import com.example.capstone.util.chatbot.recreate.FestivalRecreatePromptBuilder;
import com.example.capstone.util.chatbot.recreate.SpotRecreatePromptBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
public class ChatBotController {

    // 프롬프트 빌더
    private final SpotPromptBuilder spotPromptBuilder;
    private final FestivalPromptBuilder festivalPromptBuilder;
    private final SpotRecreatePromptBuilder spotRecreatePromptBuilder;
    private final FestivalRecreatePromptBuilder festivalRecreatePromptBuilder;

    // GPT 호출 및 파서
    private final OpenAiClient openAiClient;
    private final ChatBotParseService parseService;

    // 서비스
    private final DestinationChatService destinationChatService;
    private final GpsChatService gpsChatService;
    private final DestinationChatRecreateService destinationChatRecreateService;
    private final GpsChatRecreateService gpsChatRecreateService;

    /**
     * 목적지 기반 질문 처리
     */
    @PostMapping("/destination")
    public ResponseEntity<?> getInfoByDestination(@RequestBody ChatBotReqDto request) throws Exception {
        City city = request.getCity();
        ChatCategory category = request.getCategory();

        return switch (category) {
            case SPOT -> ResponseEntity.ok(destinationChatService.getSpot(city));
            case FESTIVAL -> ResponseEntity.ok(destinationChatService.getFestivalList(city));
            case FOOD -> ResponseEntity.ok(destinationChatService.getFoodList(city));
            case HOTEL -> ResponseEntity.ok(destinationChatService.getHotelList(city));
            case WEATHER -> ResponseEntity.ok(destinationChatService.getWeather(city));
        };
    }

    /**
     * 현위치 기반 질문 처리
     */
    @PostMapping("/gps")
    public ResponseEntity<?> getInfoByLocation(@RequestBody ChatBotGpsReqDto request) throws Exception {
        double lat = request.getLatitude();
        double lng = request.getLongitude();
        ChatCategory category = request.getCategory();

        return switch (category) {
            case SPOT -> ResponseEntity.ok(gpsChatService.getSpotList(lat, lng));
            case FESTIVAL -> ResponseEntity.ok(gpsChatService.getFestivalList(lat, lng));
            case FOOD -> ResponseEntity.ok(gpsChatService.getFoodList(lat, lng));
            case HOTEL -> ResponseEntity.ok(gpsChatService.getHotelList(lat, lng));
            case WEATHER -> ResponseEntity.ok(gpsChatService.getWeather(lat, lng));
        };
    }

    @PostMapping("/recreate/destination")
    public ResponseEntity<?> recreateChatInfo(@RequestBody ChatBotRecreateReqDto req) {
        ChatCategory category = req.getCategory();

        try {
            return switch (category) {
                case SPOT -> ResponseEntity.ok(destinationChatRecreateService.recreateSpot(req));
                case FESTIVAL -> ResponseEntity.ok(destinationChatRecreateService.recreateFestival(req));
                case FOOD -> ResponseEntity.ok(destinationChatRecreateService.recreateFood(req));
                case HOTEL -> ResponseEntity.ok(destinationChatRecreateService.recreateHotel(req));
                default -> ResponseEntity.badRequest().body("지원하지 않는 카테고리입니다.");
            };
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("재조회 처리 중 오류 발생");
        }
    }

    @PostMapping("/recreate/gps")
    public ResponseEntity<?> recreateByGps(@RequestBody ChatBotGpsRecreateReqDto request) {
        double lat = request.getLatitude();
        double lng = request.getLongitude();
        ChatCategory category = request.getCategory();
        List<String> excluded = request.getExcludedNames();

        try {
            return switch (category) {
                case SPOT -> ResponseEntity.ok(gpsChatRecreateService.recreateSpot(lat, lng, excluded));
                case FESTIVAL -> ResponseEntity.ok(gpsChatRecreateService.recreateFestival(lat, lng, excluded));
                case FOOD -> ResponseEntity.ok(gpsChatRecreateService.recreateFood(lat, lng, excluded));
                case HOTEL -> ResponseEntity.ok(gpsChatRecreateService.recreateHotel(lat, lng, excluded));
                default -> ResponseEntity.badRequest().body("지원하지 않는 카테고리입니다.");
            };
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("GPS 재조회 처리 중 오류 발생");
        }
    }


}

