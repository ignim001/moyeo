package com.example.capstone.matching.repository;

import com.example.capstone.matching.entity.MatchingProfile;

import java.util.List;

public interface MatchingProfileRepositoryCustom {
    List<MatchingProfile> matchingProfile(MatchingProfile profile);
}
