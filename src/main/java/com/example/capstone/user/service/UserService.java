package com.example.capstone.service;

import com.example.capstone.dto.oauth2.CustomOAuth2User;
import com.example.capstone.user.dto.UserProfileReqDto;
import com.example.capstone.user.dto.UserProfileResDto;
import com.example.capstone.user.domain.UserEntity;
import com.example.capstone.exception.DuplicateNicknameException;
import com.example.capstone.exception.UserNotFoundException;
import com.example.capstone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ImageService imageService;

    @Value("${default.image-url}")
    private String DEFAULT_PROFILE_IMAGE_URL;

    @Transactional
    public UserEntity signup(CustomOAuth2User userDetails, UserProfileReqDto dto, MultipartFile profileImage) {
        // 닉네임 중복 처리
        if (userRepository.existsByNickname(dto.getNickname())) {
            throw new DuplicateNicknameException("Nickname already exists");
        }

        String imageUrl;

        if (profileImage != null || !profileImage.isEmpty()) {
            // S3 이미지 저장후 URL 저장
            imageUrl = imageService.imageUpload(profileImage);
        } else {
            imageUrl = DEFAULT_PROFILE_IMAGE_URL;
        }

        UserEntity user = UserEntity.builder()
                .providerId(userDetails.getProviderId())
                .email(userDetails.getEmail())
                .nickname(dto.getNickname())
                .age(dto.getAge())
                .gender(dto.getGender())
                .mbti(dto.getMbti())
                .profileImageUrl(imageUrl)
                .build();

        userRepository.save(user);
        return user;
    }

    @Transactional(readOnly = true)
    public UserProfileResDto findUser(CustomOAuth2User customOAuth2User){
        UserEntity user = userRepository.findByProviderId(customOAuth2User.getProviderId())
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));

        return UserProfileResDto.builder()
                .nickname(user.getNickname())
                .age(user.getAge())
                .gender(user.getGender())
                .mbti(user.getMbti())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    @Transactional
    public void updateProfile(CustomOAuth2User customOAuth2User, UserProfileReqDto dto, MultipartFile profileImage){
        UserEntity user = userRepository.findByProviderId(customOAuth2User.getProviderId())
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));

        if (userRepository.existsByNicknameAndProviderIdNot(dto.getNickname(), customOAuth2User.getProviderId())) {
            throw new DuplicateNicknameException("Nickname already exists");
        }

        String imageUrl = user.getProfileImageUrl();

        if (profileImage == null || profileImage.isEmpty()) {
            // 저장된 이미지가 기본 이미지가 아닌경우
            if (!isDefaultImage(imageUrl)) {
                imageService.deleteImage(imageUrl);
            }
            // 이미지 없는경우 기본 이미지 URL 입력
            imageUrl = DEFAULT_PROFILE_IMAGE_URL;
        } else {
            if (!isDefaultImage(imageUrl)) {
                imageService.deleteImage(imageUrl);
            }
            imageUrl = imageService.imageUpload(profileImage);
        }

        user.updateProfile(dto.getNickname(), dto.getGender(), dto.getAge(), dto.getMbti(), imageUrl);
        userRepository.save(user);
    }

    private boolean isDefaultImage(String imageUrl) {
        return DEFAULT_PROFILE_IMAGE_URL.equals(imageUrl);
    }
}
