package com.example.capstone.plan.dto.response;

import com.example.capstone.plan.dto.common.PlaceDetailDto;

import java.util.List;

public class DaySummaryResponse {
    private String date; // 날짜 (예: 2025-05-01)
    private List<PlaceDetailDto> places; // 하루 장소 리스트
    private int totalEstimatedCost; // ✅ 하루 총 예상 비용

    public DaySummaryResponse() {}

    public DaySummaryResponse(String date, List<PlaceDetailDto> places, int totalEstimatedCost) {
        this.date = date;
        this.places = places;
        this.totalEstimatedCost = totalEstimatedCost;
    }

    // Getter, Setter
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<PlaceDetailDto> getPlaces() {
        return places;
    }

    public void setPlaces(List<PlaceDetailDto> places) {
        this.places = places;
    }

    public int getTotalEstimatedCost() {
        return totalEstimatedCost;
    }

    public void setTotalEstimatedCost(int totalEstimatedCost) {
        this.totalEstimatedCost = totalEstimatedCost;
    }
}
