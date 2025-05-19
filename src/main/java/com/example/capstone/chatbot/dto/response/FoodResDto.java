package com.example.capstone.chatbot.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodResDto {
    private String name;        // 상호명
    private String menu;        // 대표 메뉴
    private String priceRange;  // 가격대
    private String location;    // 주소
    private String hours;       // 영업시간
}
