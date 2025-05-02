package com.example.capstone.plan.dto.response;

import com.example.capstone.plan.dto.common.FromPreviousDto;
import com.example.capstone.plan.dto.common.PlaceDetailDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDetailFullResponse {

    private List<PlaceResponse> places;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceResponse {
        private String name;
        private String type;
        private int estimatedCost;
        private String address;
        private Double lat;
        private Double lng;
        private String description;
        private FromPreviousDto fromPrevious; // ✅ 외부 DTO 사용
        private String gptOriginalName;

        public PlaceDetailDto toDto() {
            PlaceDetailDto dto = new PlaceDetailDto();
            dto.setName(this.name);
            dto.setType(this.type);
            dto.setEstimatedCost(this.estimatedCost);
            dto.setAddress(this.address);
            dto.setLat(this.lat);
            dto.setLng(this.lng);
            dto.setDescription(this.description);
            dto.setFromPrevious(this.fromPrevious); // ✅ 타입 일치 → 그대로 할당 가능
            dto.setGptOriginalName(this.gptOriginalName);
            return dto;
        }
        public static PlaceResponse from(PlaceDetailDto dto) {
            PlaceResponse response = new PlaceResponse();
            response.setName(dto.getName());
            response.setType(dto.getType());
            response.setEstimatedCost(dto.getEstimatedCost());
            response.setAddress(dto.getAddress());
            response.setLat(dto.getLat());
            response.setLng(dto.getLng());
            response.setDescription(dto.getDescription());
            response.setFromPrevious(dto.getFromPrevious()); // FromPreviousDto 타입 동일
            response.setGptOriginalName(dto.getGptOriginalName());
            return response;
        }

    }

    public List<PlaceDetailDto> toPlaceDetailDtoList() {
        return this.places.stream()
                .map(PlaceResponse::toDto)
                .toList();
    }
}
