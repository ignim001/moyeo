package com.example.capstone.chatbot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatBotGpsReq {
    private String category;     // 예: "관광지", "맛집", "숙소" 등
    private String userInput;    // 자연어 질문 (예: "근처에 혼밥하기 좋은 백반집 알려줘")
    private double latitude;     // 사용자 GPS 위도
    private double longitude;    // 사용자 GPS 경도
}
