package com.example.capstone.plan.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiClient {

    @Value("${openai.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private WebClient webClient;

    // 생성자에서 WebClient 초기화
    public void initialize() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String callGpt(String prompt) {
        if (webClient == null) {
            initialize(); // 처음 요청 시 초기화
        }

        try {
            String requestBody = objectMapper.writeValueAsString(
                    Map.of(
                            "model", "gpt-4o",
                            "messages", List.of(
                                    Map.of("role", "user", "content", prompt)
                            )
                    )
            );

            String responseBody = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode json = objectMapper.readTree(responseBody);
            return json.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 처리 중 오류", e);
        }
    }
}
