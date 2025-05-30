package com.example.capstone.community.dto;

import com.example.capstone.matching.entity.City;
import com.example.capstone.matching.entity.Province;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostReqDto {

    @NotBlank
    private String title;
    @NotBlank
    private String content;
    @NotBlank
    private Province province;
    private City city;
}
