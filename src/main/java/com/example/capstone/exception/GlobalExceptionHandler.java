package com.example.capstone.exception;

import com.example.capstone.matching.exception.MatchingProfileNotFoundException;
import com.example.capstone.user.exception.DuplicateNicknameException;
import com.example.capstone.user.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({UserNotFoundException.class, DuplicateNicknameException.class, MatchingProfileNotFoundException.class})
    public ResponseEntity<ErrorDetails> handleUserException(RuntimeException ex, WebRequest request) {
        ErrorDetails errorDetails =
                new ErrorDetails(ex.getMessage(),
                        request.getDescription(false),
                        null);

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

}
