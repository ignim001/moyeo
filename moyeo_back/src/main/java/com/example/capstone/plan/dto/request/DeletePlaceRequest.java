package com.example.capstone.plan.dto.request;

import com.example.capstone.plan.dto.common.PlaceDetailDto;

import java.util.List;

public class DeletePlaceRequest {
    private List<String> placeNamesToDelete;
    private List<PlaceDetailDto> partialSchedule;

    public DeletePlaceRequest() {}

    public List<String> getPlaceNamesToDelete() {
        return placeNamesToDelete;
    }

    public void setPlaceNamesToDelete(List<String> placeNamesToDelete) {
        this.placeNamesToDelete = placeNamesToDelete;
    }

    public List<PlaceDetailDto> getPartialSchedule() {
        return partialSchedule;
    }

    public void setPartialSchedule(List<PlaceDetailDto> partialSchedule) {
        this.partialSchedule = partialSchedule;
    }
}
