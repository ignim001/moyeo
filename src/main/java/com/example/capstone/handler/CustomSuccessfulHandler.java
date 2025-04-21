package com.example.capstone.handler;

import com.example.capstone.dto.oauth2.CustomOAuth2User;
import com.example.capstone.util.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomSuccessfulHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String providerId = oAuth2User.getProviderId();
        String email = oAuth2User.getEmail();

        String token = jwtUtil.generateToken(providerId, email);
        // JWT 토큰을 응답 헤더에 추가
        response.addHeader("Authorization", "Bearer " + token);
        // 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, "http://localhost:3000/success"); // 리다이렉트 URL 수정
    }
}
