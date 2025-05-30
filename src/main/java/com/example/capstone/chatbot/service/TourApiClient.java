package com.example.capstone.chatbot.service;

import com.example.capstone.plan.entity.City;
import com.example.capstone.plan.service.KakaoMapClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class TourApiClient {

    @Value("${TOURAPI_KEY}")
    private String apiKey;

    private final WebClient webClient = WebClient.create();
    private final KakaoMapClient kakaoMapClient;

    private JsonNode getJsonResponse(String url) {
        String responseBody = webClient.get()
                .uri(URI.create(url))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(responseBody);
        } catch (Exception e) {
            throw new RuntimeException("TourAPI JSON 파싱 실패", e);
        }
    }

    public JsonNode getFestivalList(double lat, double lng, LocalDate today) {
        City city = kakaoMapClient.getCityFromLatLng(lat, lng);
        String url = String.format(
                "https://apis.data.go.kr/B551011/KorService2/searchFestival2?serviceKey=%s&MobileOS=ETC&MobileApp=MyApp&eventStartDate=%s&areaCode=%d&numOfRows=10&pageNo=1&_type=json",
                apiKey,
                today.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                city.getAreaCode()
        );
        return getJsonResponse(url);
    }

    public JsonNode getFestivalListByCity(City city, LocalDate today) {
        String url = String.format(
                "https://apis.data.go.kr/B551011/KorService2/searchFestival2?serviceKey=%s&MobileOS=ETC&MobileApp=MyApp&eventStartDate=%s&areaCode=%d&numOfRows=10&pageNo=1&_type=json",
                apiKey,
                today.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                city.getAreaCode()
        );
        return getJsonResponse(url);
    }
    // 특정 도시의 축제 목록 가져오기, 특정 이름 제외
    public JsonNode getFestivalListByCityExcluding(City city, LocalDate today, List<String> excludedNames) {
        JsonNode rawJson = getFestivalListByCity(city, today);
        JsonNode itemsNode = rawJson.at("/response/body/items/item");
        if (itemsNode.isMissingNode() || !itemsNode.isArray()) {
            throw new RuntimeException("TourAPI 축제 목록이 없습니다.");
        }

        List<JsonNode> filteredList = StreamSupport.stream(itemsNode.spliterator(), false)
                .filter(f -> {
                    String title = f.get("title").asText();
                    return !excludedNames.contains(title);
                })
                .limit(3)
                .collect(Collectors.toList());

        ObjectMapper mapper = new ObjectMapper();
        return mapper.valueToTree(filteredList);
    }
    //GPS 기반으로 축제 목록 가져오기, 특정 이름 제외
    public JsonNode getFestivalListByGpsExcluding(double lat, double lng, LocalDate today, List<String> excludedNames) {
        // 위도/경도를 City로 변환
        City city = kakaoMapClient.getCityFromLatLng(lat, lng);
        JsonNode rawJson = getFestivalList(lat, lng, today);  // 이미 areaCode 포함
        JsonNode itemsNode = rawJson.at("/response/body/items/item");
        if (itemsNode.isMissingNode() || !itemsNode.isArray()) {
            throw new RuntimeException("TourAPI 축제 목록이 없습니다.");
        }

        List<JsonNode> filteredList = StreamSupport.stream(itemsNode.spliterator(), false)
                .filter(f -> {
                    String title = f.get("title").asText();
                    return excludedNames.stream().noneMatch(e -> title.equalsIgnoreCase(e));
                })
                .limit(3)
                .collect(Collectors.toList());

        ObjectMapper mapper = new ObjectMapper();
        return mapper.valueToTree(filteredList);
    }


}