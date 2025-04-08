package com.example.capstone.dto.oauth2;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuth2DTO {

    private String providerId;
    private String email;
}
