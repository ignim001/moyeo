package com.example.capstone.service;

import com.example.capstone.dto.oauth2.CustomOAuth2User;
import com.example.capstone.dto.request.MatchingProfileReqDto;
import com.example.capstone.dto.response.MatchingListProfileResDto;
import com.example.capstone.dto.response.MatchingUserProfileResDto;
import com.example.capstone.entity.MatchCity;
import com.example.capstone.entity.MatchTravelStyle;
import com.example.capstone.entity.MatchingProfile;
import com.example.capstone.user.domain.UserEntity;
import com.example.capstone.exception.MatchingProfileNotFoundException;
import com.example.capstone.user.exception.UserNotFoundException;
import com.example.capstone.repository.MatchingProfileRepository;
import com.example.capstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final UserRepository userRepository;
    private final MatchingProfileRepository matchingProfileRepository;

    // 매칭 정보 생성, 수정
    @Transactional
    public void createMatchProfile(CustomOAuth2User userDetails, MatchingProfileReqDto profileRequestDto) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));

        Optional<MatchingProfile> optionalProfile = matchingProfileRepository.findByUser(user);

        if (optionalProfile.isPresent()) {
            MatchingProfile profile = optionalProfile.get();

            // 기존 매칭정보 업데이트
            profile.updateProfile(
                    profileRequestDto.getStartDate(),
                    profileRequestDto.getEndDate(),
                    profileRequestDto.getProvince(),
                    profileRequestDto.getGroupType(),
                    profileRequestDto.getAgeRange()
            );

            // 기존 여행 성향 초기화 후 새로 추가
            profile.getTravelStyles().clear();
            profile.getMatchCities().clear();
            updateTravelStyle(profileRequestDto, profile);
            updateCity(profileRequestDto, profile);
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
                .build();

        updateTravelStyle(profileRequestDto, newProfile);
        updateCity(profileRequestDto, newProfile);
        matchingProfileRepository.save(newProfile);
    }

    private void updateTravelStyle(MatchingProfileReqDto profileRequestDto, MatchingProfile profile) {
        if (profileRequestDto.getTravelStyles() != null) {
            profile.getTravelStyles().addAll(
                    profileRequestDto.getTravelStyles().stream()
                            .map(style -> new MatchTravelStyle(profile, style))
                            .collect(Collectors.toList())
            );
        }
    }

    private void updateCity(MatchingProfileReqDto profileRequestDto, MatchingProfile profile) {
        if (profileRequestDto.getTravelStyles() != null) {
            profile.getMatchCities().addAll(
                    profileRequestDto.getCities().stream()
                            .map(city -> new MatchCity(profile, city))
                            .collect(Collectors.toList())
            );
        }
    }

    // 매칭된 사용자 상세 정보 조회
    @Transactional(readOnly = true)
    public MatchingUserProfileResDto matchingUserProfile(String nickname) {
        UserEntity user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));

        MatchingProfile matchingProfile = matchingProfileRepository.findByUser(user)
                .orElseThrow(() -> new MatchingProfileNotFoundException("Matching profile not found"));

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
                        .collect(Collectors.toList()))
                .travelStyles(matchingProfile.getTravelStyles().stream()
                        .map(MatchTravelStyle::getTravelStyle)
                        .collect(Collectors.toList()))
                .build();
    }

    // 매칭된 사용자 목록 조회
    @Transactional(readOnly = true)
    public List<MatchingListProfileResDto> matchingResult(CustomOAuth2User userDetails) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));

        MatchingProfile profile = matchingProfileRepository.findByUser(user)
                .orElseThrow(() -> new MatchingProfileNotFoundException("Matching profile not found"));
        
        // 사용자 매칭정보와 유사한 매칭정보 조회
        List<MatchingProfile> matchingProfiles = matchingProfileRepository.matchingProfile(profile);

        return matchingProfiles.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private MatchingListProfileResDto convertToResponse(MatchingProfile profile) {
        // 매칭 프로필을 DTO로 변환하는 로직 (페치조인 사용해 최적화)
        return new MatchingListProfileResDto(
                profile.getUser().getNickname(),
                profile.getUser().getProfileImageUrl(),
                profile.getStartDate(),
                profile.getEndDate(),
                profile.getTravelStyles().stream()
                        .map(MatchTravelStyle::getTravelStyle)
                        .collect(Collectors.toList())
        );
    }
}
