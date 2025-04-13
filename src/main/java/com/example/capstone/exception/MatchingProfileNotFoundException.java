package com.example.capstone.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MatchingProfileNotFoundException extends RuntimeException {
    public MatchingProfileNotFoundException(String message) {
        super(message);
    }
}
