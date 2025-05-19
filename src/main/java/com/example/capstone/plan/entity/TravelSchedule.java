package com.example.capstone.plan.entity;

import com.example.capstone.user.entity.UserEntity;
import com.example.capstone.util.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelSchedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private String title;

    private LocalDate startDate;

    private LocalDate endDate;

    @OneToMany(mappedBy = "travelSchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TravelDay> travelDays;

}
