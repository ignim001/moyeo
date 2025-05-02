package com.example.capstone.dto.oauth2;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;

    public KakaoResponse(Map<String, Object> attributes) {
        this.attributes = Map.copyOf(attributes);
        this.kakaoAccount = (Map<String, Object>) attributes.getOrDefault("kakao_account", Map.of());
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.getOrDefault("id", ""));
    }

    @Override
    public String getEmail() {
        return String.valueOf(kakaoAccount.getOrDefault("email", ""));
    }
}
