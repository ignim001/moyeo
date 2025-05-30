package com.example.capstone.community.dto;

import com.example.capstone.community.entity.Post;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Builder
public class PostListResDto {

    private String nickname;
    private String title;
    private LocalDateTime createdAt;
    private int countComment;
    private String firstImage;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public PostListResDto(String nickname, String title, LocalDateTime createdAt, int countComment, String imageJson) {
        this.nickname = nickname;
        this.title = title;
        this.createdAt = createdAt;
        this.countComment = countComment;
        this.firstImage = extractFirstImage(imageJson);
    }

    private String extractFirstImage(String imageJson) {
        try {
            List<String> images = objectMapper.readValue(imageJson, new TypeReference<>() {});
            return (images != null && !images.isEmpty()) ? images.get(0) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
