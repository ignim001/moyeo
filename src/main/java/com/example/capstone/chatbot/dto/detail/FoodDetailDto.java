package com.example.capstone.chatbot.dto.detail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FoodDetailDto {
    private String menu;         // 대표 메뉴 (예: "제육볶음")
    private String priceRange;   // 가격대 (예: "1인 10,000~15,000원")
    private String hours; // 영업시간 (예: "매일 10:00~21:00")
}
