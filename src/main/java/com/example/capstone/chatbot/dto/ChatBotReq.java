package com.example.capstone.chatbot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatBotReq {
    private String category;   // 예: "관광지", "맛집", "숙소", "축제", "날씨"
    private String userInput;  // 사용자의 자연어 질문
}
