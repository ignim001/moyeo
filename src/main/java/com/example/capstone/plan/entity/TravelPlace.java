package com.example.capstone.plan.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;  // 장소 이름

    private String type;  // 식사/관광지/액티비티/숙소 등

    private String address;  // 장소 주소

    private Double lat;   // 위도

    private Double lng;   // 경도

    private String description;  // 한 줄 소개

    private Integer estimatedCost;  // 예상 비용 (원 단위)

    private String gptOriginalName;  // GPT가 추천한 원래 이름

    private Integer placeOrder;  // 하루 안에서 방문 순서 (1, 2, 3...)

    @Embedded
    private FromPrevious fromPrevious;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_day_id")
    private TravelDay travelDay;
}
