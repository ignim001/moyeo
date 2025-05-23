package com.example.capstone.chatbot.dto.response;

import com.example.capstone.chatbot.dto.detail.FoodDetailDto;
import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodResDto {
    private String name;        // 상호명
    private String menu;        // 대표 메뉴
    private String priceRange;  // 가격대
    private String location;    // 주소
    private String hours;       // 영업시간


}
