package com.example.capstone.chatbot.dto.request;

import com.example.capstone.chatbot.entity.ChatCategory;
import com.example.capstone.plan.entity.City;
import lombok.Data;


@Data
public class ChatBotReqDto {
    private City city;
    private ChatCategory category;
}
