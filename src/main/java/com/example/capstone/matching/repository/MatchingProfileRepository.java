package com.example.capstone.matching.repository;

import com.example.capstone.matching.entity.MatchingProfile;
import com.example.capstone.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchingProfileRepository extends JpaRepository<MatchingProfile, Long>, MatchingProfileRepositoryCustom {
    Optional<MatchingProfile> findByUser(UserEntity user);
    boolean existsByUser(UserEntity user);
}
