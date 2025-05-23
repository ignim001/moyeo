package com.example.capstone.chatbot.dto.detail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HotelDetailDto {
    private String priceRange;  // "90,000원 ~ 120,000원"
    private String checkIn;     // "15:00"
    private String checkOut;    // "11:00"
}
