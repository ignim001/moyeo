package com.example.capstone.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentListResDto {

    private String nickname;
    private String userProfile;
    private String comment;
    private LocalDateTime updatedAt;
}
