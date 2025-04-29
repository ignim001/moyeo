package com.example.capstone.util.oauth2.dto;

import java.util.Map;

public class GoogleResponse implements OAuth2Response {

    private final Map<String, Object> attributes;

    public GoogleResponse(Map<String, Object> attributes) {
        this.attributes = Map.copyOf(attributes);
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.getOrDefault("sub", ""));
    }

    @Override
    public String getEmail() {
        return String.valueOf(attributes.getOrDefault("email", ""));
    }

}
