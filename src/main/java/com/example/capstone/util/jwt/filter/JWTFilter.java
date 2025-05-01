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
        CustomOAuth2User customOAuth2User;
        // 임시 토큰 검증 (회원 가입 시)
        if (requestURI.startsWith("/auth/signup")){
            if (token == null || !jwtUtil.validateJwt(token) || !jwtUtil.getTypeFromJwt(token).equals("TEMP")){
                filterChain.doFilter(request, response);
                return;
            }
            String providerId = jwtUtil.getProviderIdFromJwt(token);
            String email = jwtUtil.getEmailFromJwt(token);

            customOAuth2User = new CustomOAuth2User(OAuth2DTO.builder()
                    .providerId(providerId)
                    .email(email)
                    .build());
        } 
        // 정식 토큰 검증 (회원 가입 외 모든 요청)
        else {
            if (token == null || !jwtUtil.validateJwt(token) || !jwtUtil.getTypeFromJwt(token).equals("ACCESS")) {
                filterChain.doFilter(request, response);
                return;
            }
            String providerId = jwtUtil.getProviderIdFromJwt(token);
            String email = jwtUtil.getEmailFromJwt(token);
            String nickname = jwtUtil.getNicknameFromJwt(token);

            customOAuth2User = new CustomOAuth2User(OAuth2DTO.builder()
                    .providerId(providerId)
                    .email(email)
                    .nickname(nickname)
                    .build());
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
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
