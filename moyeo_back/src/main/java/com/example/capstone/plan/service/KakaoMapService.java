package com.example.capstone.plan.service;

import com.example.capstone.plan.dto.common.KakaoPlaceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class KakaoMapService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public KakaoPlaceDto searchPlace(String query) {
        String url = UriComponentsBuilder.fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                .queryParam("query", query)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                var json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response.getBody());
                var first = json.get("documents").get(0);
                String name = first.get("place_name").asText();
                double lon = first.get("x").asDouble();
                double lat = first.get("y").asDouble();
                String address = first.has("road_address_name") && !first.get("road_address_name").isNull()
                        ? first.get("road_address_name").asText()
                        : first.get("address_name").asText();
                String categoryGroupCode = first.has("category_group_code") && !first.get("category_group_code").isNull()
                        ? first.get("category_group_code").asText()
                        : "";

                return new KakaoPlaceDto(name, lat, lon, address, categoryGroupCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
