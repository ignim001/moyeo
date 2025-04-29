package com.example.capstone.service;

import com.example.capstone.dto.oauth2.*;
import com.example.capstone.user.domain.UserEntity;
import com.example.capstone.user.repository.UserRepository;
import com.example.capstone.util.JwtUtil;

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
    private final JwtUtil jwtUtil;

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

        String providerId = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        UserEntity existUser = userRepository.findByProviderId(providerId).orElse(null);

        // 기존 사용자인 경우 임시 토큰 포함 X
        if (existUser != null) {
            return new CustomOAuth2User(OAuth2DTO.builder()
                    .providerId(existUser.getProviderId())
                    .email(existUser.getEmail())
                    .build());
        }

        // 신규 회원인 경우 임시 토큰 포함 O
        String tempToken = jwtUtil.generateToken(providerId, oAuth2Response.getEmail());
        return new CustomOAuth2User(OAuth2DTO.builder()
                .providerId(providerId)
                .email(oAuth2Response.getEmail())
                .tempToken(tempToken)
                .build());
    }


}

