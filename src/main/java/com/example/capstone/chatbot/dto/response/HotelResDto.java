package com.example.capstone.chatbot.dto.response;

import com.example.capstone.chatbot.dto.detail.HotelDetailDto;
import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelResDto {
    private String name;        // 숙소명
    private String priceRange;  // 1박 기준 가격대
    private String address;     // 주소
    private String phone;       // 연락처 (없으면 null)
    private String checkIn;     // 체크인 시간
    private String checkOut;    // 체크아웃 시간


}
