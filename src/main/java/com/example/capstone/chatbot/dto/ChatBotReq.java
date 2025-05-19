package com.example.capstone.chatbot.dto;

import com.example.capstone.chatbot.entity.ChatCategory;
import com.example.capstone.plan.entity.City;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatBotReq {
    private City city;
    private ChatCategory category;
}
