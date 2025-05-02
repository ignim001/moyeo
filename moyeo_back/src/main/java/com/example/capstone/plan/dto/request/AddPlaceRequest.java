package com.example.capstone.plan.dto.request;

import com.example.capstone.plan.dto.common.PlaceDetailDto;

import java.util.List;

public class AddPlaceRequest {
    private String newPlaceName;
    private int insertIndex; // 추가할 위치
    private List<PlaceDetailDto> partialSchedule;

    public AddPlaceRequest() {}

    public String getNewPlaceName() {
        return newPlaceName;
    }

    public void setNewPlaceName(String newPlaceName) {
        this.newPlaceName = newPlaceName;
    }

    public int getInsertIndex() {
        return insertIndex;
    }

    public void setInsertIndex(int insertIndex) {
        this.insertIndex = insertIndex;
    }

    public List<PlaceDetailDto> getPartialSchedule() {
        return partialSchedule;
    }

    public void setPartialSchedule(List<PlaceDetailDto> partialSchedule) {
        this.partialSchedule = partialSchedule;
    }
}
