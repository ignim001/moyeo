package com.example.capstone.chatbot.dto.request;

import com.example.capstone.chatbot.entity.ChatCategory;
import com.example.capstone.plan.entity.City;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatBotRecreateReqDto {
    private City city;                      // 또는 GPS 기반 double lat, lng
    private ChatCategory category;
    private List<String> excludedNames;
}
