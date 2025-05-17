package com.example.capstone.user.service;

import com.example.capstone.util.oauth2.dto.CustomOAuth2User;
import com.example.capstone.util.s3.ImageService;
import com.example.capstone.user.dto.UserProfileReqDto;
import com.example.capstone.user.dto.UserProfileResDto;
import com.example.capstone.user.entity.UserEntity;
import com.example.capstone.user.exception.DuplicateNicknameException;
import com.example.capstone.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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

    private static final String USER_IMAGE_DIR = "user-image";

    @Transactional
    public UserEntity signup(CustomOAuth2User userDetails, UserProfileReqDto dto, MultipartFile profileImage) {
        // 닉네임 중복 처리
        if (userRepository.existsByNickname(dto.getNickname())) {
            throw new DuplicateNicknameException("Nickname already exists");
        }

        String imageUrl;

        if (profileImage != null && !profileImage.isEmpty()) {
            // user-image 디렉토리에 저장
            imageUrl = imageService.imageUpload(profileImage, USER_IMAGE_DIR);
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
    public UserProfileResDto findUser(CustomOAuth2User customOAuth2User) {
        UserEntity user = userRepository.findByProviderId(customOAuth2User.getProviderId())
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        return UserProfileResDto.builder()
                .nickname(user.getNickname())
                .age(user.getAge())
                .gender(user.getGender())
                .mbti(user.getMbti())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    @Transactional
    public void updateProfile(CustomOAuth2User customOAuth2User, UserProfileReqDto dto, MultipartFile profileImage) {
        UserEntity user = userRepository.findByProviderId(customOAuth2User.getProviderId())
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        if (userRepository.existsByNicknameAndProviderIdNot(dto.getNickname(), customOAuth2User.getProviderId())) {
            throw new DuplicateNicknameException("Nickname already exists");
        }

        String imageUrl = user.getProfileImageUrl();

        if (profileImage == null || profileImage.isEmpty()) {
            // 기존 이미지가 기본 이미지가 아닌 경우 삭제
            if (!isDefaultImage(imageUrl)) {
                imageService.deleteImage(imageUrl);
            }
            imageUrl = DEFAULT_PROFILE_IMAGE_URL;
        } else {
            // 기존 이미지 삭제
            if (!isDefaultImage(imageUrl)) {
                imageService.deleteImage(imageUrl);
            }
            // 새 이미지 업로드
            imageUrl = imageService.imageUpload(profileImage, USER_IMAGE_DIR);
        }

        user.updateProfile(dto.getNickname(), dto.getGender(), dto.getAge(), dto.getMbti(), imageUrl);
        userRepository.save(user);
    }

    private boolean isDefaultImage(String imageUrl) {
        return DEFAULT_PROFILE_IMAGE_URL.equals(imageUrl);
    }
}
