package com.example.capstone.plan.service;


import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KakaoMapClient {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private WebClient getWebClient() {
        return WebClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoApiKey)
                .build();
    }

    /**
     * GPT 응답 기반 정제: location.name 우선 → 실패 시 name fallback
     */
    public KakaoPlaceDto searchPlaceFromGpt(String gptName, String locationName, String categoryCode) {
        // 1. location.name 우선 시도
        if (locationName != null && !locationName.isBlank()) {
            KakaoPlaceDto result = searchPlaceWithCategory(locationName, categoryCode);
            if (result != null) {
                System.out.println("🎯 location.name 으로 검색 성공: " + locationName);
                return result;
            }
            System.out.println("⛔ location.name 검색 실패 → fallback 진행");
        }

        // 2. fallback: gpt가 작성한 일반 이름
        return searchPlaceWithCategory(gptName, categoryCode);
    }

    /**
     * 기본 검색 (카테고리 코드 없이)
     */
    public KakaoPlaceDto searchPlace(String keyword) {
        return searchPlaceWithCategory(keyword, null);
    }

    /**
     * 카테고리 코드 기반 장소 검색 (정확한 코드 일치 항목만 추출)
     */
    public KakaoPlaceDto searchPlaceWithCategory(String keyword, String categoryCode) {
        try {
            WebClient.RequestHeadersUriSpec<?> request = getWebClient().get();

            WebClient.RequestHeadersSpec<?> uriSpec = request.uri(uriBuilder -> {
                var builder = uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", keyword);

                if (categoryCode != null && !categoryCode.isBlank()) {
                    builder.queryParam("category_group_code", categoryCode);
                }

                return builder.build();
            });

            String response = uriSpec
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("📡 Kakao API 응답 원문: " + response);
            JsonNode json = objectMapper.readTree(response);
            JsonNode documents = json.get("documents");

            if (documents != null && documents.size() > 0) {
                for (JsonNode doc : documents) {
                    String docCategory = doc.has("category_group_code") ? doc.get("category_group_code").asText() : "";

                    if (categoryCode == null || categoryCode.isBlank()
                            || categoryCode.equals(docCategory)) {

                        String name = doc.get("place_name").asText();
                        double lat = doc.get("y").asDouble();
                        double lon = doc.get("x").asDouble();
                        String address = doc.has("road_address_name") && !doc.get("road_address_name").isNull()
                                ? doc.get("road_address_name").asText()
                                : doc.get("address_name").asText();

                        System.out.println("✅ 장소 찾음: " + name);
                        return new KakaoPlaceDto(name, lat, lon, address, docCategory); // ✅ 이 부분 수정됨
                    }
                }

                // fallback: 첫 번째 결과라도 반환
                JsonNode fallback = documents.get(0);
                String name = fallback.get("place_name").asText();
                double lat = fallback.get("y").asDouble();
                double lon = fallback.get("x").asDouble();
                String address = fallback.has("road_address_name") && !fallback.get("road_address_name").isNull()
                        ? fallback.get("road_address_name").asText()
                        : fallback.get("address_name").asText();
                String fallbackCategory = fallback.has("category_group_code") ? fallback.get("category_group_code").asText() : "";

                System.out.println("⚠️ categoryCode 조건 불일치, 첫 결과 fallback: " + name);
                return new KakaoPlaceDto(name, lat, lon, address, fallbackCategory); // ✅ 이 부분도 수정됨
            }


            System.out.println("❌ 일치하는 장소 없음: " + keyword + " [" + categoryCode + "]");

        } catch (Exception e) {
            System.err.println("❌ Kakao API 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public KakaoPlaceDto searchPlaceByKeyword(String keyword) {
        return searchPlaceWithCategory(keyword, null);
    }


}
