package com.example.capstone.service;

import com.example.capstone.dto.oauth2.*;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.exception.OAuth2Exception;
import com.example.capstone.exception.UserNotFoundException;
import com.example.capstone.repository.UserRepository;
import com.example.capstone.util.JWTUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;

        if ("kakao".equals(registrationId)) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else if ("google".equals(registrationId)) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {
            throw new OAuth2AuthenticationException("Unsupported OAuth2 provider");
        }

        // OAuth2 제공자의 식별 ID 생성 (예: "kakao 123456789" or "google 987654321")
        String providerId = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        UserEntity existUser = userRepository.findByProviderId(providerId).orElse(null);

        // 기존 회원이면 바로 로그인 (JWT 발급)
        if (existUser != null) {
            return new CustomOAuth2User(OAuth2DTO.builder()
                    .providerId(existUser.getProviderId())
                    .email(existUser.getEmail())
                    .build());
        }

        // 신규 회원이면 임시 JWT 발급 (추가 정보 입력 유도)
        String tempToken = jwtUtil.generateToken(providerId, oAuth2Response.getEmail());
        System.out.println("tempToken = " + tempToken);
        throw new OAuth2Exception("Additional information required", tempToken);
    }
}

