package com.example.capstone.user.repository;

import com.example.capstone.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByProviderId(String providerId);
    Optional<UserEntity> findByNickname(String nickname);
    boolean existsByNickname(String nickname);
    boolean existsByNicknameAndProviderIdNot(String nickname, String providerId);
}
