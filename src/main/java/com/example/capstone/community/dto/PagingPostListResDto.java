package com.example.capstone.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagingPostListResDto {

    private List<PostListResDto> postListResDtos;
    private int nowPage;
    private boolean hasNextPage;
}
