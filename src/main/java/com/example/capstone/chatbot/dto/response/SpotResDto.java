package com.example.capstone.chatbot.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotResDto {
    private String name;        // 장소명
    private String description; // 한줄 설명
    private String hours;       // 운영시간
    private String fee;         // 입장료
    private String location;    // 주소
}
