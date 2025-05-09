package com.example.capstone.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatBotRes {
    private String answer;  // GPT가 생성한 자연어 응답
}
