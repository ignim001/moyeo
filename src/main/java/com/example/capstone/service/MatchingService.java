package com.example.capstone.service;

import com.example.capstone.dto.oauth2.CustomOAuth2User;
import com.example.capstone.dto.request.MatchingProfileRequest;
import com.example.capstone.dto.response.MatchingProfileResponse;
import com.example.capstone.entity.MatchTravelStyle;
import com.example.capstone.entity.MatchingProfile;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.exception.MatchingProfileNotFoundException;
import com.example.capstone.exception.UserNotFoundException;
import com.example.capstone.repository.MatchingProfileRepository;
import com.example.capstone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final UserRepository userRepository;
    private final MatchingProfileRepository matchingProfileRepository;

    @Transactional
    public void createMatchProfile(CustomOAuth2User userDetails, MatchingProfileRequest profileRequestDto) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));

        Optional<MatchingProfile> optionalProfile = matchingProfileRepository.findByUser(user);

        if (optionalProfile.isPresent()) {
            MatchingProfile profile = optionalProfile.get();

            // 기존 프로필 업데이트
            profile.updateProfile(
                    profileRequestDto.getStartDate(),
                    profileRequestDto.getEndDate(),
                    profileRequestDto.getProvince(),
                    profileRequestDto.getCity(),
                    profileRequestDto.getGroupType(),
                    profileRequestDto.getAgeRange()
            );

            // 기존 여행 성향 초기화 후 새로 추가
            profile.getTravelStyles().clear();
            updateTravelStyle(profileRequestDto, profile);
            return;
        }

        // 새 프로필 생성
        MatchingProfile newProfile = MatchingProfile.builder()
                .user(user)
                .startDate(profileRequestDto.getStartDate())
                .endDate(profileRequestDto.getEndDate())
                .province(profileRequestDto.getProvince())
                .city(profileRequestDto.getCity())
                .groupType(profileRequestDto.getGroupType())
                .ageRange(profileRequestDto.getAgeRange())
                .build();

        updateTravelStyle(profileRequestDto, newProfile);
        matchingProfileRepository.save(newProfile);
    }

    private void updateTravelStyle(MatchingProfileRequest profileRequestDto, MatchingProfile profile) {
        if (profileRequestDto.getTravelStyles() != null) {
            profile.getTravelStyles().addAll(
                    profileRequestDto.getTravelStyles().stream()
                            .map(style -> new MatchTravelStyle(profile, style))
                            .collect(Collectors.toList())
            );
        }
    }

    @Transactional(readOnly = true)
    public MatchingProfileResponse getMatchingResultProfile(String nickname) {
        UserEntity user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));

        MatchingProfile matchingProfile = matchingProfileRepository.findByUser(user)
                .orElseThrow(() -> new MatchingProfileNotFoundException("Matching profile not found"));

        return MatchingProfileResponse.builder()
                .nickname(user.getNickname())
                .imageUrl(user.getProfileImageUrl())
                .gender(user.getGender())
                .mbti(user.getMbti())
                .startDate(matchingProfile.getStartDate())
                .endDate(matchingProfile.getEndDate())
                .province(matchingProfile.getProvince())
                .city(matchingProfile.getCity())
                // Enum 타입 변환
                .travelStyles(matchingProfile.getTravelStyles().stream()
                        .map(MatchTravelStyle::getTravelStyle)
                        .collect(Collectors.toList()))
                .build();
    }

    // 업데이트 로직 분리
//    @Transactional
//    public void updateMatchProfile(CustomOAuth2User userDetails, MatchingProfileRequest profileRequestDto) {
//        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
//                .orElseThrow(() -> new UserNotFoundException("User Not Found"));
//
//        MatchingProfile profile = matchingProfileRepository.findByUser(user)
//                .orElseThrow(() -> new MatchingProfileNotFoundException("Matching profile not found"));
//
//        profile.updateProfile(
//                profileRequestDto.getStartDate(),
//                profileRequestDto.getEndDate(),
//                profileRequestDto.getProvince(),
//                profile.getCity(),
//                profile.getGroupType(),
//                profile.getAgeRange());
//
//        // 기존 여행 성향 초기화
//        profile.getTravelStyles().clear();
//
//        updateTravelStyle(profileRequestDto, profile);
//        matchingProfileRepository.save(profile);

//    }
}
