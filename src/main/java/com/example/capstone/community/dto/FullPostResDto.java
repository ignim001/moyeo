package com.example.capstone.community.dto;

import com.example.capstone.matching.entity.City;
import com.example.capstone.matching.entity.Province;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FullPostResDto {

    private Long postId;

    private String nickname;
    private String userProfileImage;

    private String title;
    private String content;
    private Province province;
    private City city;
    private List<String> postImages;
    private LocalDateTime createdDate;
}
