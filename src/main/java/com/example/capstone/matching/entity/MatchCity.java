package com.example.capstone.matching.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchCity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_profile_id")
    private MatchingProfile matchingProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private City city;

    public MatchCity(MatchingProfile matchingProfile, City city) {
        this.matchingProfile = matchingProfile;
        this.city = city;
    }
}
