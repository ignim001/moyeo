package com.example.capstone.plan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private TravelSchedule travelSchedule;  //

    @OneToMany(mappedBy = "travelDay", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TravelPlace> travelPlaces;  //

    @Column(name = "day_number")
    private Integer dayNumber;

    @Column(name = "date")
    private String date;  // YYYY-MM-DD 형식 날짜

    @Column(name = "day_label")
    private String day;  // 예: "1일차", "2일차"

}

