package com.example.capstone.chatbot.service;

import com.example.capstone.chatbot.dto.response.WeatherResDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class OpenWeatherClient {

    @Value("${openweather.api.key}")
    private String apiKey;

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    private WebClient getWebClient() {
        return webClientBuilder
                .baseUrl("https://api.openweathermap.org")
                .build();
    }

    public WeatherResDto getWeather(double lat, double lon, String regionName) {
        String url = UriComponentsBuilder
                .fromPath("/data/3.0/onecall")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("exclude", "minutely,hourly,alerts")
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .queryParam("lang", "kr")
                .toUriString();

        try {
            String response = getWebClient()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode body = objectMapper.readTree(response);

            double currentTemp = body.get("current").get("temp").asDouble();
            double minTemp = body.get("daily").get(0).get("temp").get("min").asDouble();
            double maxTemp = body.get("daily").get(0).get("temp").get("max").asDouble();
            double pop = body.get("daily").get(0).get("pop").asDouble() * 100;

            // 원본 날씨 설명
            String rawDescription = body.get("current")
                    .get("weather")
                    .get(0)
                    .get("description")
                    .asText();

            // 간소화된 날씨 설명
            String simplifiedDescription = simplifyWeatherDescription(rawDescription);

            // 요청 시간
            String requestTime = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            return new WeatherResDto(
                    regionName,
                    String.format("%.1f°C", currentTemp),
                    String.format("%.1f°C", minTemp),
                    String.format("%.1f°C", maxTemp),
                    String.format("%.0f%%", pop),
                    simplifiedDescription,
                    requestTime
            );

        } catch (Exception e) {
            throw new RuntimeException("OpenWeather API 응답 처리 중 오류", e);
        }
    }

    // 날씨 설명 간소화: 맑음, 구름, 비, 눈
    private String simplifyWeatherDescription(String description) {
        if (description.contains("구름") || description.contains("흐림")) return "구름";
        if (description.contains("비")) return "비";
        if (description.contains("눈")) return "눈";
        if (description.contains("맑")) return "맑음";
        return "기타";
    }
}
