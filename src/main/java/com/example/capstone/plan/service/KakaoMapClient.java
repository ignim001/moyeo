package com.example.capstone.plan.service;

import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class KakaoMapClient {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private WebClient webClient;

    private void initializeWebClient() {
        if (webClient == null) {
            this.webClient = WebClient.builder()
                    .baseUrl("https://dapi.kakao.com")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoApiKey)
                    .build();
        }
    }

    public KakaoPlaceDto searchPlaceFromGpt(String gptName, String locationName, String categoryCode) {
        initializeWebClient();

        if (locationName != null && !locationName.isBlank()) {
            KakaoPlaceDto result = searchPlaceWithCategory(locationName, categoryCode);
            if (result != null) return result;
        }
        return searchPlaceWithCategory(gptName, categoryCode);
    }

    public KakaoPlaceDto searchPlace(String keyword) {
        return searchPlaceWithCategory(keyword, null);
    }

    public KakaoPlaceDto searchPlaceWithCategory(String keyword, String categoryCode) {
        initializeWebClient();

        try {
            String response = webClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/v2/local/search/keyword.json")
                                .queryParam("query", keyword);
                        if (categoryCode != null && !categoryCode.isBlank()) {
                            builder.queryParam("category_group_code", categoryCode);
                        }
                        return builder.build();
                    })
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode documents = objectMapper.readTree(response).get("documents");

            if (documents != null && documents.size() > 0) {
                // 정확히 일치하는 categoryCode가 있으면 우선 반환
                for (JsonNode doc : documents) {
                    String docCategory = doc.path("category_group_code").asText("");
                    if (categoryCode == null || categoryCode.isBlank() || categoryCode.equals(docCategory)) {
                        return extractPlaceFromJson(doc);
                    }
                }
                // fallback: 첫 번째 결과라도 반환
                return extractPlaceFromJson(documents.get(0));
            }

        } catch (Exception e) {
            throw new RuntimeException("KakaoMap 검색 중 오류 발생", e);
        }

        return null;
    }

    private KakaoPlaceDto extractPlaceFromJson(JsonNode doc) {
        String name = doc.path("place_name").asText();
        double lat = doc.path("y").asDouble();
        double lon = doc.path("x").asDouble();
        String address = doc.has("road_address_name") && !doc.get("road_address_name").isNull()
                ? doc.get("road_address_name").asText()
                : doc.get("address_name").asText();
        String category = doc.path("category_group_code").asText("");
        return new KakaoPlaceDto(name, lat, lon, address, category);
    }
}
