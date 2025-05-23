package com.example.capstone.plan.service;

import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import com.example.capstone.plan.entity.City;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KakaoMapClient {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;

    private WebClient getWebClient() {
        return webClientBuilder
                .baseUrl("https://dapi.kakao.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoApiKey)
                .build();
    }

    // GPT 장소 정제용 (단일 결과)
    public KakaoPlaceDto searchPlaceFromGpt(String gptName, String locationName, String categoryCode) {
        if (locationName != null && !locationName.isBlank()) {
            KakaoPlaceDto result = searchPlaceWithCategory(locationName, categoryCode);
            if (result != null) return result;
        }
        return searchPlaceWithCategory(gptName, categoryCode);
    }

    // 일반 키워드 검색 (단일 결과)
    public KakaoPlaceDto searchPlace(String keyword) {
        return searchPlaceWithCategory(keyword, null);
    }

    // 단일 장소 검색
    public KakaoPlaceDto searchPlaceWithCategory(String keyword, String categoryCode) {
        try {
            String response = getWebClient().get()
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
                for (JsonNode doc : documents) {
                    String docCategory = doc.path("category_group_code").asText("");
                    if (categoryCode == null || categoryCode.isBlank() || categoryCode.equals(docCategory)) {
                        return extractPlaceFromJson(doc);
                    }
                }
                return extractPlaceFromJson(documents.get(0));
            }

        } catch (Exception e) {
            throw new RuntimeException("KakaoMap 검색 중 오류 발생", e);
        }

        return null;
    }

    // 다중 장소 검색 (챗봇 목적지용)
    public List<KakaoPlaceDto> searchPlacesWithCategory(String keyword, String categoryCode) {
        try {
            String response = getWebClient().get()
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
            List<KakaoPlaceDto> result = new ArrayList<>();

            if (documents != null && documents.size() > 0) {
                for (JsonNode doc : documents) {
                    result.add(extractPlaceFromJson(doc));
                }
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("KakaoMap 다중 장소 검색 중 오류 발생", e);
        }

    }

    // GPS 기반 다중 장소 검색 (반경 내 카테고리 장소)
    public List<KakaoPlaceDto> searchPlacesByCategory(double lat, double lng, String categoryCode) {
        try {
            String response = getWebClient().get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/category.json")
                            .queryParam("category_group_code", categoryCode)
                            .queryParam("x", lng)  // 경도
                            .queryParam("y", lat)  // 위도
                            .queryParam("radius", 5000)  // 5km 이내
                            .queryParam("sort", "distance") // 거리순 정렬
                            .build()
                    )
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode documents = objectMapper.readTree(response).get("documents");
            List<KakaoPlaceDto> result = new ArrayList<>();

            if (documents != null && documents.size() > 0) {
                for (JsonNode doc : documents) {
                    result.add(extractPlaceFromJson(doc));
                }
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("KakaoMap 좌표 기반 장소 검색 중 오류 발생", e);
        }
    }

    // 재조회 전용: City + 카테고리 코드 + 개수 제한
    public List<KakaoPlaceDto> searchTopPlacesByCityAndCategory(City city, String categoryCode, int limit) {
        // 수정: City에서 좌표 직접 가져오지 않고 Kakao 키워드 검색으로 추출
        KakaoPlaceDto cityCenter = searchPlace(city.getDisplayName());
        if (cityCenter == null) {
            throw new RuntimeException("도시 중심 좌표 검색 실패: " + city.getDisplayName());
        }
        return searchTopPlacesByCategory(cityCenter.getLatitude(), cityCenter.getLongitude(), categoryCode, limit);
    }

    // 내부적으로 호출할 좌표 기반 로직
    public List<KakaoPlaceDto> searchTopPlacesByCategory(double lat, double lng, String categoryCode, int limit) {
        try {
            String response = getWebClient().get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/category.json")
                            .queryParam("category_group_code", categoryCode)
                            .queryParam("x", lng)
                            .queryParam("y", lat)
                            .queryParam("radius", 5000)
                            .queryParam("sort", "distance")
                            .build()
                    )
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode documents = objectMapper.readTree(response).get("documents");
            List<KakaoPlaceDto> result = new ArrayList<>();

            if (documents != null && documents.size() > 0) {
                for (JsonNode doc : documents) {
                    result.add(extractPlaceFromJson(doc));
                    if (result.size() >= limit) break;
                }
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("KakaoMap 재조회 전용 검색 오류", e);
        }
    }

    // JSON → DTO 변환 공통 메서드
    private KakaoPlaceDto extractPlaceFromJson(JsonNode doc) {
        String name = doc.path("place_name").asText();
        double lat = doc.path("y").asDouble();
        double lon = doc.path("x").asDouble();
        String address = doc.has("road_address_name") && !doc.get("road_address_name").isNull()
                ? doc.get("road_address_name").asText()
                : doc.get("address_name").asText();
        String phone = doc.path("phone").asText("");
        String category = doc.path("category_group_code").asText("");
        return new KakaoPlaceDto(name, lat, lon, address, phone, category);
    }
}
