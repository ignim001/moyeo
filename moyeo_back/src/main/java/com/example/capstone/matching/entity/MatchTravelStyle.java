package com.example.capstone.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchTravelStyle {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_profile_id")
    private MatchingProfile matchingProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TravelStyle travelStyle;

    public MatchTravelStyle(MatchingProfile matchingProfile, TravelStyle travelStyle) {
        this.matchingProfile = matchingProfile;
        this.travelStyle = travelStyle;
    }
}
