package com.example.capstone.chatbot.dto;

import com.example.capstone.chatbot.entity.ChatCategory;
import lombok.Data;


@Data
public class ChatBotGpsReq {
    private ChatCategory category;  // 예: SPOT, FOOD, HOTEL, FESTIVAL, WEATHER
    private double latitude;        // 사용자 GPS 위도
    private double longitude;       // 사용자 GPS 경도
}
