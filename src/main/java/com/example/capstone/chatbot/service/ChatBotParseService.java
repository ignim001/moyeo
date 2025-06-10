package com.example.capstone.chatbot.service;

import com.example.capstone.chatbot.dto.response.*;
import com.example.capstone.chatbot.entity.ChatCategory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatBotParseService {

    private final ObjectMapper objectMapper;

    public Object parseResponse(ChatCategory category, String gptResponse) throws Exception {
        return switch (category) {
            case SPOT -> parse(gptResponse, new TypeReference<SpotResDto>() {});
            case FOOD -> parse(gptResponse, new TypeReference<List<FoodResDto>>() {});
            case HOTEL -> parse(gptResponse, new TypeReference<List<HotelResDto>>() {});
            case FESTIVAL -> parse(gptResponse, new TypeReference<FestivalResDto>() {});
            case WEATHER -> parse(gptResponse, new TypeReference<WeatherResDto>() {});
        };

    }

    public  <T> T parse(String json, TypeReference<T> typeRef) throws Exception {
        return objectMapper.readValue(json, typeRef);
    }
}
