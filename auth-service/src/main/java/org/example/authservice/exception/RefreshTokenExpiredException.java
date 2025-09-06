package org.example.authservice.exception;

import org.springframework.http.HttpStatus;

public class RefreshTokenExpiredException extends ControllerException {
    public RefreshTokenExpiredException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}

