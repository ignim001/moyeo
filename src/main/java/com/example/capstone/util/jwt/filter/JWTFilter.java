package com.example.capstone.util.jwt.filter;

import com.example.capstone.util.oauth2.dto.CustomOAuth2User;
import com.example.capstone.util.oauth2.dto.OAuth2DTO;
import com.example.capstone.util.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        
        // 해당 경로 검증 X
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/oauth2/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = parseJwt(request);
        log.info("Method: {}, URI: {}, Token: {}", request.getMethod(), request.getRequestURI(), token);
        if (token == null || !jwtUtil.validateJwt(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰에서 사용자 정보를 가져옴
        String providerId = jwtUtil.getProviderIdFromJwt(token);
        String email = jwtUtil.getEmailFromJwt(token);

        // 사용자 정보를 기반으로 OAuth2User 생성
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(OAuth2DTO.builder()
                .providerId(providerId)
                .email(email)
                .build());

        // 스프링 시큐리티 인증 토큰 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        // SecurityContext 인증 정보 설정
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 필터 체인 진행
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

}
