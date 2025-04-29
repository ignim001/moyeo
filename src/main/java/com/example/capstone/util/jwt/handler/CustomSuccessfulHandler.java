package com.example.capstone.util.jwt.handler;

import com.example.capstone.util.oauth2.dto.CustomOAuth2User;
import com.example.capstone.util.jwt.JwtUtil;
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

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String providerId = oAuth2User.getProviderId();
        String email = oAuth2User.getEmail();

        String token = jwtUtil.generateToken(providerId, email);

        // 세션에서 redirect_uri 가져오기
        String redirectUri = (String) request.getSession().getAttribute("redirect_uri");
        if (redirectUri == null) {
            redirectUri = "exp://default-url"; // fallback 주소
        }

        // 신규, 기존 사용자 구분해 redirect
        if (oAuth2User.getTempToken() != null) {
            response.sendRedirect(redirectUri + "?mode=register&token=" + oAuth2User.getTempToken());
        } else {
            String jwtToken = jwtUtil.generateToken(oAuth2User.getProviderId(), oAuth2User.getEmail());
            response.sendRedirect(redirectUri + "?mode=login&token=" + jwtToken);
        }
    }
}
