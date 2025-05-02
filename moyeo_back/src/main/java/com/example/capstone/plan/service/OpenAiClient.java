package com.example.capstone.plan.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class OpenAiClient {

    @Value("${openai.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String callGpt(String prompt) {
        try {
            // WebClient는 apiKey가 주입된 후에 생성해야 함!
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://api.openai.com/v1/chat/completions")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            // 요청 바디 구성
            String rawJson = webClient.post()
                    .bodyValue(
                            java.util.Map.of(
                                    "model", "chatgpt-4o-latest",
                                    "messages", java.util.List.of(
                                            java.util.Map.of(
                                                    "role", "user",
                                                    "content", prompt
                                            )
                                    )
                            )
                    )
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // JSON 파싱
            JsonNode node = objectMapper.readTree(rawJson);

            // 안전하게 응답 탐색
            return node.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
