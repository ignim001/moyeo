package com.example.capstone.config;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest originalRequest = defaultResolver.resolve(request);
        return customize(request, originalRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest originalRequest = defaultResolver.resolve(request, clientRegistrationId);
        return customize(request, originalRequest);
    }

    private OAuth2AuthorizationRequest customize(HttpServletRequest request, OAuth2AuthorizationRequest originalRequest) {
        if (originalRequest == null) return null;

        // redirect_uri 파라미터 추출
        String redirectUriFromClient = request.getParameter("redirect_uri");
        if (redirectUriFromClient != null) {
            // 세션 또는 쿠키에 저장
            HttpSession session = request.getSession();
            session.setAttribute("redirect_uri", redirectUriFromClient);
        }

        return originalRequest;
    }
}
