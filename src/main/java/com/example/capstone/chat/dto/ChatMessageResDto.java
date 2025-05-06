package com.example.capstone.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageResDto {
    private String sender;
    private String message;
    private LocalDateTime timestamp;
    private Long unReadUserCount;
}
