package com.example.capstone.entity;

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
    private CITY city;

    public MatchCity(MatchingProfile matchingProfile, CITY city) {
        this.matchingProfile = matchingProfile;
        this.city = city;
    }
}
