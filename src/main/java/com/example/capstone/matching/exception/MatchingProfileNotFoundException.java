package com.example.capstone.matching.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MatchingProfileNotFoundException extends RuntimeException {
    public MatchingProfileNotFoundException(String message) {
        super(message);
    }
}
