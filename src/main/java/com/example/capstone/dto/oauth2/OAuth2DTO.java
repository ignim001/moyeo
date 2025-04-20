package com.example.capstone.dto.oauth2;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth2DTO {

    private String providerId;
    private String email;
}
