package com.example.capstone.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OAuth2Exception extends RuntimeException {
    private final String tempToken;

    public OAuth2Exception(String message, String tempToken) {
        super(message);
        this.tempToken = tempToken;
    }
}

