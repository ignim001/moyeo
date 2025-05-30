package com.example.capstone.chatbot.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FestivalResDto {
    private String name;       // 축제명
    private String period;       // 개최 기간
    private String location;   // 장소
    private String highlight;  // 주요 행사 내용
    private String fee;     // 입장료 여부 (무료 / 유료)
}
