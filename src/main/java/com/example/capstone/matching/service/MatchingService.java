package com.example.capstone.matching.service;

import com.example.capstone.util.oauth2.dto.CustomOAuth2User;
import com.example.capstone.matching.dto.MatchingProfileReqDto;
import com.example.capstone.matching.dto.MatchingListProfileResDto;
import com.example.capstone.matching.dto.MatchingUserProfileResDto;
import com.example.capstone.matching.entity.MatchCity;
import com.example.capstone.matching.entity.MatchTravelStyle;
import com.example.capstone.matching.entity.MatchingProfile;
import com.example.capstone.user.entity.UserEntity;
import com.example.capstone.matching.repository.MatchingProfileRepository;
import com.example.capstone.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final UserRepository userRepository;
    private final MatchingProfileRepository matchingProfileRepository;

    // 매칭 정보 생성, 수정
    @Transactional
    public void createMatchProfile(CustomOAuth2User userDetails, MatchingProfileReqDto profileRequestDto) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        Optional<MatchingProfile> optionalProfile = matchingProfileRepository.findByUser(user);

        if (optionalProfile.isPresent()) {
            MatchingProfile profile = optionalProfile.get();

            // 기존 매칭정보 업데이트
            profile.updateProfile(
                    profileRequestDto.getStartDate(),
                    profileRequestDto.getEndDate(),
                    profileRequestDto.getProvince(),
                    profileRequestDto.getGroupType(),
                    profileRequestDto.getAgeRange(),
                    profileRequestDto.getPreferenceGender()
            );

            // 기존 여행 성향 초기화 후 새로 추가
            profile.getTravelStyles().clear();
            profile.getMatchCities().clear();
            mapToTravelStyle(profileRequestDto, profile);
            mapToCity(profileRequestDto, profile);
            return;
        }

        // 새 매칭정보 생성
        MatchingProfile newProfile = MatchingProfile.builder()
                .user(user)
                .startDate(profileRequestDto.getStartDate())
                .endDate(profileRequestDto.getEndDate())
                .province(profileRequestDto.getProvince())
                .groupType(profileRequestDto.getGroupType())
                .ageRange(profileRequestDto.getAgeRange())
                .preferenceGender(profileRequestDto.getPreferenceGender())
                .build();

        mapToTravelStyle(profileRequestDto, newProfile);
        mapToCity(profileRequestDto, newProfile);
        matchingProfileRepository.save(newProfile);
    }

    private void mapToTravelStyle(MatchingProfileReqDto profileRequestDto, MatchingProfile profile) {
        if (profileRequestDto.getTravelStyles() != null) {
            profile.getTravelStyles().addAll(
                    profileRequestDto.getTravelStyles().stream()
                            .map(style -> new MatchTravelStyle(profile, style))
                            .toList()
            );
        }
    }

    private void mapToCity(MatchingProfileReqDto profileRequestDto, MatchingProfile profile) {
        if (profileRequestDto.getTravelStyles() != null) {
            profile.getMatchCities().addAll(
                    profileRequestDto.getCities().stream()
                            .map(city -> new MatchCity(profile, city))
                            .toList()
            );
        }
    }

    // 매칭된 사용자 상세 정보 조회
    @Transactional(readOnly = true)
    public MatchingUserProfileResDto matchingUserProfile(String nickname) {
        UserEntity user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        MatchingProfile matchingProfile = matchingProfileRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Matching profile not found"));

        return MatchingUserProfileResDto.builder()
                .nickname(user.getNickname())
                .imageUrl(user.getProfileImageUrl())
                .gender(user.getGender())
                .mbti(user.getMbti())
                .startDate(matchingProfile.getStartDate())
                .endDate(matchingProfile.getEndDate())
                .province(matchingProfile.getProvince())
                // Enum 타입 변환
                .cities(matchingProfile.getMatchCities().stream()
                        .map(MatchCity::getCity)
                        .toList())
                .travelStyles(matchingProfile.getTravelStyles().stream()
                        .map(MatchTravelStyle::getTravelStyle)
                        .toList())
                .build();
    }

    // 매칭된 사용자 목록 조회
    @Transactional(readOnly = true)
    public List<MatchingListProfileResDto> matchingResult(CustomOAuth2User userDetails) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        MatchingProfile profile = matchingProfileRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Matching profile not found"));
        
        // 사용자 매칭정보와 유사한 매칭정보 조회
        List<MatchingProfile> matchingProfiles = matchingProfileRepository.matchingProfile(profile);

        return matchingProfiles.stream()
                .map(this::convertToResponse)
                .toList();
    }

    // 매칭 프로필 DTO 변환 로직 (페치조인 사용해 최적화)
    private MatchingListProfileResDto convertToResponse(MatchingProfile profile) {
        return new MatchingListProfileResDto(
                profile.getUser().getNickname(),
                profile.getUser().getProfileImageUrl(),
                profile.getStartDate(),
                profile.getEndDate(),
                profile.getTravelStyles().stream()
                        .map(MatchTravelStyle::getTravelStyle)
                        .toList()
        );
    }
}
