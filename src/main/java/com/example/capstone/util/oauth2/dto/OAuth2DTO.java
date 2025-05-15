package com.example.capstone.util.oauth2.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth2DTO {

    private String providerId;
    private String email;
    private String tempToken;
    private String nickname;
    private Long userId;
}
