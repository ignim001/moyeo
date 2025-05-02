package com.example.capstone.dto.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2DTO oAuth2DTO;

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getName() {
        return oAuth2DTO.getProviderId();
    }

    public String getProviderId() {
        return oAuth2DTO.getProviderId();
    }

    public String getEmail() {
        return oAuth2DTO.getEmail();
    }

    public String getTempToken() {return oAuth2DTO.getTempToken();}
}
